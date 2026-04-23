const mongoose = require('mongoose');

const emailLogSchema = new mongoose.Schema({
    emailId: {
        type: String,
        required: true,
        unique: true,
        index: true
    },
    to: {
        type: String,
        required: true
    },
    from: {
        type: String,
        required: true
    },
    subject: {
        type: String,
        required: true
    },
    type: {
        type: String,
        enum: ['verification', 'report'],
        required: true
    },
    sent: {
        type: Boolean,
        default: false
    },
    sentAt: {
        type: Date
    },
    opened: {
        type: Boolean,
        default: false
    },
    openedAt: {
        type: Date
    },
    openCount: {
        type: Number,
        default: 0
    },
    lastOpenedAt: {
        type: Date
    },
    metadata: {
        type: Map,
        of: String
    },
    error: {
        type: String
    }
}, {
    timestamps: true
});

emailLogSchema.index({ createdAt: -1 });
emailLogSchema.index({ type: 1, createdAt: -1 });
emailLogSchema.index({ to: 1, createdAt: -1 });

module.exports = mongoose.model('EmailLog', emailLogSchema);
