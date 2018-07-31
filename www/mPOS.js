var exec = require('cordova/exec');

function formatTransactionInfo(report) {
	return Object.assign({}, report, {
		isSign: Boolean(report.isSign),
		isPinEntered: Boolean(report.isPinEntered)
	});
}

var mPOS = {
	activateManagerAndGetAvailableDevices: function() {
		return new Promise(function(resolve, reject) {
			return exec(
				function(devices) {
					return resolve(
						devices.map(function(device) {
							var splitted = device.split(' - ');
							return {
								address: splitted[0],
								name: splitted[1],
								id: device
							};
						})
					);
				},
				reject,
				'mPOS',
				'activateManagerAndGetAvailableDevices'
			);
		});
	},
	connect: function(deviceName) {
		return new Promise(function(resolve, reject) {
			return exec(resolve, reject, 'mPOS', 'connect', [deviceName]);
		});
	},
	disconnect: function() {
		return new Promise(function(resolve, reject) {
			return exec(resolve, reject, 'mPOS', 'disconnect');
		});
	},
	isHaveConnectedReader: function() {
		return new Promise(function(resolve, reject) {
			return exec(resolve, reject, 'mPOS', 'isHaveConnectedReader', []);
		});
	},
	payment: function(amount) {
		return new Promise(function(resolve, reject) {
			return exec(
				function(report) {
					return resolve(formatTransactionInfo(report));
				},
				reject,
				'mPOS',
				'payment',
				[amount]
			);
		});
	},
	getReaderInfo: function() {
		return new Promise(function(resolve, reject) {
			return exec(resolve, reject, 'mPOS', 'getReaderInfo', []);
		});
	},
	closeDayAndGetReport: function() {
		return new Promise(function(resolve, reject) {
			return exec(resolve, reject, 'mPOS', 'closeDayAndGetReport', []);
		});
	},
	getPaymentStatus: function(transactionId) {
		return new Promise(function(resolve, reject) {
			return exec(
				function(report) {
					return resolve(formatTransactionInfo(report));
				},
				reject,
				'mPOS',
				'getPaymentStatus',
				[transactionId]
			);
		});
	}
};

module.exports = mPOS;
