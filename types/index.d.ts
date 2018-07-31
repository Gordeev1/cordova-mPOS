interface Window {
	mPOS: mPOS;
}

type Device = {
	name: string;
	address: string;
	id: string;
};

type ReaderInfo = {
	ksn: string;
	readerVersion: string;
	serialNumber: string;
};

type TransactionInfo = {
	transactionId: string;
	authorizationCode: string;
	cardExpiryDate: string;
	cardNumberHash: string;
	maskedCardNumber: string;
	paymentSystemName: string;
	receipt: string;
	rrn: string;
	terminalNumber: string;
	transactionNumber: string;
	operationDateTime: string;
	isPinEntered: boolean;
	isSign: boolean;
	resultCode: string;
	requestId: string;
	cardEntryTypeName?: string;
	cardEntryTypeCode?: string;
	errorMessage?: string;
};

type ReconciliationReport = {
	receipt: string;
	resultCode: string;
	terminalNumber: string;
};

/**
 * The mPOS object provides methods to connect and manage ICMP or D200 UPOS devices.
 */
interface mPOS {
	activateManagerAndGetAvailableDevices: () => Promise<Device[]>;
	connect: (deviceID: string) => Promise<void>;
	disconnect: () => Promise<void>;
	isHaveConnectedReader: () => Promise<boolean>;
	payment: (amount: string) => Promise<TransactionInfo>;
	getPaymentStatus: (transactionId: string) => Promise<TransactionInfo>;
	getReaderInfo: () => Promise<ReaderInfo>;
	closeDayAndGetReport: () => Promise<ReconciliationReport>;
}

declare var mPOS: mPOS;
