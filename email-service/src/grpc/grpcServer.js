const grpc = require('@grpc/grpc-js');
const protoLoader = require('@grpc/proto-loader');
const path = require('path');
const EmailService = require('../services/emailService');

const PROTO_PATH = path.join(__dirname, 'email.proto');

// Load proto file
const packageDefinition = protoLoader.loadSync(PROTO_PATH, {
    keepCase: true,
    longs: String,
    enums: String,
    defaults: true,
    oneofs: true
});

const emailProto = grpc.loadPackageDefinition(packageDefinition).email;

class GrpcServer {
    constructor() {
        this.server = new grpc.Server();
        this.emailService = new EmailService();
        this.setupServices();
    }

    setupServices() {
        this.server.addService(emailProto.EmailService.service, {
            SendVerificationEmail: this.sendVerificationEmail.bind(this),
            SendReportEmail: this.sendReportEmail.bind(this),
            GetEmailStatus: this.getEmailStatus.bind(this)
        });
    }

    async sendVerificationEmail(call, callback) {
        const { to, username, verificationCode, verificationLink } = call.request;

        console.log(`gRPC Request: SendVerificationEmail to ${to}`);

        try {
            const result = await this.emailService.sendVerificationEmail(
                to,
                username,
                verificationCode,
                verificationLink
            );

            callback(null, {
                success: result.success,
                message: result.message,
                emailId: result.emailId
            });

        } catch (error) {
            console.error('gRPC Error in SendVerificationEmail:', error);
            callback({
                code: grpc.status.INTERNAL,
                message: error.message
            });
        }
    }

    async sendReportEmail(call, callback) {
        const { to, username, reportTitle, reportContent, reportDate, reportData } = call.request;

        console.log(`gRPC Request: SendReportEmail to ${to}`);

        try {
            // Convert reportData from proto format to array
            const dataArray = reportData ? reportData.map(item => ({
                key: item.key,
                value: item.value
            })) : [];

            const result = await this.emailService.sendReportEmail(
                to,
                username,
                reportTitle,
                reportContent,
                reportDate,
                dataArray
            );

            callback(null, {
                success: result.success,
                message: result.message,
                emailId: result.emailId
            });

        } catch (error) {
            console.error('gRPC Error in SendReportEmail:', error);
            callback({
                code: grpc.status.INTERNAL,
                message: error.message
            });
        }
    }

    async getEmailStatus(call, callback) {
        const { emailId } = call.request;

        console.log(`gRPC Request: GetEmailStatus for ${emailId}`);

        try {
            const result = await this.emailService.getEmailStatus(emailId);

            if (!result.found) {
                callback({
                    code: grpc.status.NOT_FOUND,
                    message: result.message
                });
                return;
            }

            callback(null, {
                sent: result.sent,
                opened: result.opened,
                openedAt: result.openedAt || '',
                openCount: result.openCount
            });

        } catch (error) {
            console.error('gRPC Error in GetEmailStatus:', error);
            callback({
                code: grpc.status.INTERNAL,
                message: error.message
            });
        }
    }

    start(port = 50051) {
        return new Promise((resolve, reject) => {
            const address = `0.0.0.0:${port}`;

            this.server.bindAsync(
                address,
                grpc.ServerCredentials.createInsecure(),
                (error, boundPort) => {
                    if (error) {
                        console.error('Failed to bind gRPC server:', error);
                        reject(error);
                        return;
                    }

                    this.server.start();
                    console.log(`gRPC Server running on port ${boundPort}`);
                    resolve(boundPort);
                }
            );
        });
    }

    stop() {
        return new Promise((resolve) => {
            this.server.tryShutdown(() => {
                console.log('gRPC Server stopped');
                resolve();
            });
        });
    }
}

module.exports = GrpcServer;
