const jwt = require('jsonwebtoken');
const logger = require('../config/logger');

const JWT_SECRET = process.env.JWT_SECRET;

class AuthMiddleware {
    /**
     * Verify JWT token from socket handshake
     */
    static verifyToken(socket, next) {
        try {
            const token = socket.handshake.auth.token || socket.handshake.headers.authorization?.replace('Bearer ', '');

            if (!token) {
                logger.warn('No token provided');
                return next(new Error('Authentication error: No token provided'));
            }

            const decoded = jwt.verify(token, JWT_SECRET);

            socket.userId = decoded.userId;
            socket.username = decoded.sub;
            socket.email = decoded.email;

            logger.info(`User authenticated: ${socket.username} (ID: ${socket.userId})`);
            next();
        } catch (error) {
            logger.error('Token verification failed:', error.message);
            next(new Error('Authentication error: Invalid token'));
        }
    }
}

module.exports = AuthMiddleware;
