require('dotenv').config();
const express = require('express');
const { createServer } = require('http');
const { Server } = require('socket.io');
const cors = require('cors');
const logger = require('./config/logger');
const AuthMiddleware = require('./middleware/auth');
const rabbitMQService = require('./services/rabbitmq');
const NotificationService = require('./services/notification');

const app = express();
const httpServer = createServer(app);

app.use(cors({
    origin: process.env.CORS_ORIGIN || '*',
    credentials: true
}));

app.use(express.json());

app.get('/health', (req, res) => {
    res.json({
        status: 'ok',
        service: 'notification-ws-service',
        rabbitmq: rabbitMQService.isConnected,
        timestamp: new Date().toISOString()
    });
});

const io = new Server(httpServer, {
    cors: {
        origin: process.env.CORS_ORIGIN || '*',
        credentials: true
    },
    transports: ['websocket', 'polling']
});

io.use(AuthMiddleware.verifyToken);

const notificationService = new NotificationService(io, rabbitMQService);

// Socket.IO connection handling
io.on('connection', async (socket) => {
    logger.info(`New connection: ${socket.id} (User: ${socket.username})`);

    // Handle connection
    await notificationService.handleConnection(socket);

    // Handle custom events
    socket.on('ping', () => {
        socket.emit('pong', { timestamp: Date.now() });
    });

    socket.on('get_stats', () => {
        socket.emit('stats', notificationService.getStats());
    });

    // Handle disconnection
    socket.on('disconnect', () => {
        notificationService.handleDisconnection(socket);
    });

    // Handle errors
    socket.on('error', (error) => {
        logger.error(`Socket error for user ${socket.userId}:`, error);
    });
});

// API endpoint to get service stats (for monitoring)
app.get('/stats', (req, res) => {
    res.json(notificationService.getStats());
});

// Start server
const PORT = process.env.PORT || 4000;

async function start() {
    try {
        // Connect to RabbitMQ
        await rabbitMQService.connect();

        // Start consuming notifications from RabbitMQ
        await rabbitMQService.consumeNotifications((notification) => {
            notificationService.handleNotification(notification);
        });

        httpServer.listen(PORT, () => {
            logger.info(`WebSocket Notification Service running on port ${PORT}`);
            logger.info(`Environment: ${process.env.NODE_ENV || 'development'}`);
        });
    } catch (error) {
        logger.error('Failed to start server:', error);
        process.exit(1);
    }
}



start();
