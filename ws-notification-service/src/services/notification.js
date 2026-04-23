const logger = require('../config/logger');

class NotificationService {
    constructor(io, rabbitMQ) {
        this.io = io;
        this.rabbitMQ = rabbitMQ;
        this.userSockets = new Map();
        this.projectRooms = new Map(); 
    }


    async handleConnection(socket) {
        const userId = socket.userId;

        // Track user socket
        if (!this.userSockets.has(userId)) {
            this.userSockets.set(userId, new Set());
        }
        this.userSockets.get(userId).add(socket.id);

        logger.info(`User ${userId} connected with socket ${socket.id}`);

        try {
            const userProjects = await this.getUserProjectsViaRPC(userId);

            await this.subscribeUserToChannels(socket, userId, userProjects);

            // Emit connection success
            socket.emit('connected', {
                userId,
                message: 'Successfully connected to notification service',
                channels: this.getUserChannels(userId, userProjects)
            });

        } catch (error) {
            logger.error(`Error handling connection for user ${userId}:`, error);
            socket.emit('error', { message: 'Failed to setup notification channels' });
        }
    }

    /**
     * Subscribe user to all relevant channels
     */
    async subscribeUserToChannels(socket, userId, userProjects) {
        const personalChannel = `user:${userId}`;
        socket.join(personalChannel);
        logger.info(`User ${userId} joined personal channel: ${personalChannel}`);

        // 2. Broadcast channel
        socket.join('broadcast');

        // 3. Project-based channels
        for (const project of userProjects) {
            const projectId = project.projectId;
            const role = project.role;

            // Project-User channel: project:{projectId}:user:{userId}
            const projectUserChannel = `project:${projectId}:user:${userId}`;
            socket.join(projectUserChannel);

            // Project-Role channel: project:{projectId}:role:{role}
            const projectRoleChannel = `project:${projectId}:role:${role}`;
            socket.join(projectRoleChannel);

            // General project channel: project:{projectId}
            const projectChannel = `project:${projectId}`;
            socket.join(projectChannel);

            logger.info(`User ${userId} joined project ${projectId} channels with role ${role}`);

            // Track project membership
            if (!this.projectRooms.has(projectId)) {
                this.projectRooms.set(projectId, new Set());
            }
            this.projectRooms.get(projectId).add(userId);
        }
    }

    /**
     * Get user channels list
     */
    getUserChannels(userId, userProjects) {
        const channels = [
            `user:${userId}`,
            'broadcast'
        ];

        for (const project of userProjects) {
            channels.push(`project:${project.projectId}`);
            channels.push(`project:${project.projectId}:user:${userId}`);
            channels.push(`project:${project.projectId}:role:${project.role}`);
        }

        return channels;
    }

    /**
     * Get user projects via RabbitMQ RPC
     */
    async getUserProjectsViaRPC(userId) {
        try {
            const projects = await this.rabbitMQ.getUserProjects(userId);
            return projects || [];
        } catch (error) {
            logger.error(`Failed to get projects for user ${userId}:`, error);
            return [];
        }
    }

    /**
     * Handle notification from RabbitMQ
     */
    async handleNotification(notification) {
        logger.info('Processing notification:', notification);

        const {
            targetType,
            targetId,
            userId,
            projectId,
            role,
            data
        } = notification;

        try {
            switch (targetType) {
                case 'USER':
                    await this.sendToUser(targetId, data);
                    break;
                case 'PROJECT':
                    await this.sendToProject(projectId, data);
                    break;
                case 'PROJECT_ROLE':
                    await this.sendToProjectRole(projectId, role, data);
                    break;
                case 'PROJECT_USER':
                    await this.sendToProjectUser(projectId, userId, data);
                    break;
                case 'BROADCAST':
                    await this.broadcast(data);
                    break;
                default:
                    logger.warn(`Unknown target type: ${targetType}`);
            }
        } catch (error) {
            logger.error('Error sending notification:', error);
        }
    }

    /**
     * Send notification to specific user
     */
    async sendToUser(userId, data) {
        const channel = `user:${userId}`;
        this.io.to(channel).emit('notification', data);
        logger.info(`Sent notification to user ${userId}`);
    }

    /**
     * Send notification to all users in a project
     */
    async sendToProject(projectId, data) {
        const channel = `project:${projectId}`;
        this.io.to(channel).emit('notification', data);
        logger.info(`Sent notification to project ${projectId}`);
    }

    /**
     * Send notification to users with specific role in project
     */
    async sendToProjectRole(projectId, role, data) {
        const channel = `project:${projectId}:role:${role}`;
        this.io.to(channel).emit('notification', data);
        logger.info(`Sent notification to project ${projectId} role ${role}`);
    }

    /**
     * Send notification to specific user in project
     */
    async sendToProjectUser(projectId, userId, data) {
        const channel = `project:${projectId}:user:${userId}`;
        this.io.to(channel).emit('notification', data);
        logger.info(`Sent notification to project ${projectId} user ${userId}`);
    }

    /**
     * Broadcast to all connected users
     */
    async broadcast(data) {
        this.io.to('broadcast').emit('notification', data);
        logger.info('Broadcast notification sent to all users');
    }

    /**
     * Handle user disconnection
     */
    handleDisconnection(socket) {
        const userId = socket.userId;

        if (this.userSockets.has(userId)) {
            this.userSockets.get(userId).delete(socket.id);

            // Remove user from map if no more sockets
            if (this.userSockets.get(userId).size === 0) {
                this.userSockets.delete(userId);
            }
        }

        logger.info(`User ${userId} disconnected (socket ${socket.id})`);
    }

    /**
     * Get statistics
     */
    getStats() {
        return {
            connectedUsers: this.userSockets.size,
            totalConnections: Array.from(this.userSockets.values())
                .reduce((sum, sockets) => sum + sockets.size, 0),
            activeProjects: this.projectRooms.size
        };
    }
}

module.exports = NotificationService;
