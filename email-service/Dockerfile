FROM node:18-alpine

WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm install --production

# Copy application files
COPY src/ ./src/
COPY public/ ./public/

# Expose ports
EXPOSE 3000 50051

# Start the application
CMD ["node", "src/index.js"]
