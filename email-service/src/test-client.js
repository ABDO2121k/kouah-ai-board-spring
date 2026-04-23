const grpc = require('@grpc/grpc-js');
const protoLoader = require('@grpc/proto-loader');
const path = require('path');

const PROTO_PATH = path.join(__dirname, 'grpc', 'email.proto');

// Load proto file
const packageDefinition = protoLoader.loadSync(PROTO_PATH, {
    keepCase: true,
    longs: String,
    enums: String,
    defaults: true,
    oneofs: true
});

const emailProto = grpc.loadPackageDefinition(packageDefinition).email;

// Create gRPC client
const client = new emailProto.EmailService(
    'localhost:50051',
    grpc.credentials.createInsecure()
);

console.log('🧪 Testing Email Service via gRPC...\n');

// Test 1: Send Verification Email
function testVerificationEmail() {
    return new Promise((resolve, reject) => {
        console.log('Test 1: Sending Verification Email...');

        const request = {
            to: 'test@example.com',
            username: 'John Doe',
            verificationCode: '123456',
            verificationLink: 'https://example.com/verify?token=abc123'
        };

        client.SendVerificationEmail(request, (error, response) => {
            if (error) {
                console.error('Error:', error.message);
                reject(error);
                return;
            }

            console.log('Response:', response);
            console.log(`   Email ID: ${response.emailId}`);
            console.log(`   Success: ${response.success}`);
            console.log(`   Message: ${response.message}\n`);

            resolve(response);
        });
    });
}

// Test 2: Send Report Email
function testReportEmail() {
    return new Promise((resolve, reject) => {
        console.log('Test 2: Sending Report Email...');

        const request = {
            to: 'test@example.com',
            username: 'Jane Smith',
            reportTitle: 'Monthly Activity Report',
            reportContent: 'Here is your comprehensive monthly activity report with all the key metrics and insights.',
            reportDate: new Date().toISOString().split('T')[0],
            reportData: [
                { key: 'Total Users', value: '1,234' },
                { key: 'Active Sessions', value: '567' },
                { key: 'Revenue', value: '$12,345' },
                { key: 'Growth Rate', value: '+23%' }
            ]
        };

        client.SendReportEmail(request, (error, response) => {
            if (error) {
                console.error('Error:', error.message);
                reject(error);
                return;
            }

            console.log('Response:', response);
            console.log(`   Email ID: ${response.emailId}`);
            console.log(`   Success: ${response.success}`);
            console.log(`   Message: ${response.message}\n`);

            resolve(response);
        });
    });
}

// Test 3: Get Email Status
function testGetEmailStatus(emailId) {
    return new Promise((resolve, reject) => {
        console.log('Test 3: Getting Email Status...');

        const request = { emailId };

        client.GetEmailStatus(request, (error, response) => {
            if (error) {
                console.error('Error:', error.message);
                reject(error);
                return;
            }

            console.log('Response:', response);
            console.log(`   Sent: ${response.sent}`);
            console.log(`   Opened: ${response.opened}`);
            console.log(`   Open Count: ${response.openCount}`);
            console.log(`   Opened At: ${response.openedAt || 'Not opened yet'}\n`);

            resolve(response);
        });
    });
}

// Run all tests
async function runTests() {
    try {
        // Test verification email
        const verificationResult = await testVerificationEmail();

        // Wait a bit
        await new Promise(resolve => setTimeout(resolve, 1000));

        // Test report email
        const reportResult = await testReportEmail();

        // Wait a bit
        await new Promise(resolve => setTimeout(resolve, 1000));

        // Test getting status for the first email
        if (verificationResult.emailId) {
            await testGetEmailStatus(verificationResult.emailId);
        }

        console.log('All tests completed!');
        console.log('\nNotes:');
        console.log('   - Check your email inbox for the test emails');
        console.log('   - Open the emails to trigger the tracking pixel');
        console.log('   - Run GetEmailStatus again to see updated open counts');

        process.exit(0);

    } catch (error) {
        console.error('Test failed:', error);
        process.exit(1);
    }
}

runTests();
