const amqp = require('amqplib');
const logger = require('../config/logger');

// Module state
let connection = null;
let channel = null;
let isConnected = false;


async function connect() {
    try {
        const rabbitmqUrl = process.env.RABBITMQ_URL || 'amqp://localhost:5672';
        logger.info(`Connecting to RabbitMQ at ${rabbitmqUrl}...`);

        connection = await amqp.connect(rabbitmqUrl);
        channel = await connection.createChannel();
        isConnected = true;

        // Setup exchanges and queues
        await setupExchangesAndQueues();

        // Handle connection events
        connection.on('error', (err) => {
            logger.error('RabbitMQ connection error:', err);
            isConnected = false;
        });

        connection.on('close', () => {
            logger.warn('RabbitMQ connection closed');
            isConnected = false;
            setTimeout(() => connect(), 5000);
        });

        logger.info('Successfully connected to RabbitMQ');
    } catch (error) {
        logger.error('Failed to connect to RabbitMQ:', error);
        isConnected = false;
        setTimeout(() => connect(), 5000);
    }
}


async function setupExchangesAndQueues() {
    const notificationExchange = process.env.NOTIFICATION_EXCHANGE || 'notification.exchange';
    const notificationQueue = process.env.NOTIFICATION_QUEUE || 'notification.queue';

    // Declare exchange
    await channel.assertExchange(notificationExchange, 'topic', { durable: true });

    // Declare queue
    await channel.assertQueue(notificationQueue, { durable: true });

    await channel.bindQueue(notificationQueue, notificationExchange, 'notification.*');

    logger.info('RabbitMQ exchanges and queues setup complete');
}


async function getUserProjects(userId) {
    if (!isConnected) {
        throw new Error('RabbitMQ not connected');
    }

    return new Promise(async (resolve, reject) => {
        try {
            // Create response queue
            const { queue } = await channel.assertQueue('', { exclusive: true });
            const correlationId = generateUuid();

            // Set up consumer for response
            channel.consume(queue, (msg) => {
                if (msg.properties.correlationId === correlationId) {
                    const response = JSON.parse(msg.content.toString());
                    resolve(response);
                }
            }, { noAck: true });

            // Send request
            channel.sendToQueue('user.projects.request',
                Buffer.from(JSON.stringify({ userId })), {
                correlationId,
                replyTo: queue
            }
            );

            // Timeout after 5 seconds
            setTimeout(() => reject(new Error('Request timeout')), 5000);
        } catch (error) {
            reject(error);
        }
    });
}

/**
 * Consume notification messages
 */
async function consumeNotifications(callback) {
    if (!isConnected) {
        throw new Error('RabbitMQ not connected');
    }

    const notificationQueue = process.env.NOTIFICATION_QUEUE || 'notification.queue';

    await channel.consume(notificationQueue, async (msg) => {
        if (msg) {
            try {
                const notification = JSON.parse(msg.content.toString());
                logger.info('Received notification:', notification);

                await callback(notification);

                channel.ack(msg);
            } catch (error) {
                logger.error('Error processing notification:', error);
                channel.nack(msg, false, false);
            }
        }
    });

    logger.info('Started consuming notifications from queue');
}

/**
 * Publish notification
 */
async function publishNotification(notification) {
    if (!isConnected) {
        throw new Error('RabbitMQ not connected');
    }

    const notificationExchange = process.env.NOTIFICATION_EXCHANGE || 'notification.exchange';
    const routingKey = `notification.${notification.type || 'general'}`;

    channel.publish(
        notificationExchange,
        routingKey,
        Buffer.from(JSON.stringify(notification)),
        { persistent: true }
    );

    logger.info(`Published notification with routing key: ${routingKey}`);
}

/**
 * Generate UUID for correlation
 */
function generateUuid() {
    return Math.random().toString(36).substring(2, 15) +
        Math.random().toString(36).substring(2, 15);
}

/**
 * Close connection
 */
async function close() {
    if (channel) {
        await channel.close();
    }
    if (connection) {
        await connection.close();
    }
    isConnected = false;
    logger.info('RabbitMQ connection closed');
}

module.exports = {
    connect,
    getUserProjects,
    consumeNotifications,
    publishNotification,
    close
};
