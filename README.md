# Cordova MPOS

Cordova mPOS is a plugin for [m4bank sdk](http://m4bank.ru/mpos) provides methods to connect and manage ICMP or D200 UPOS devices.

# Installation

1.  Get mPOS lib from [vendor](http://m4bank.ru/) and add it to `<project_root>/libs/android/mPOS`
    ![like this](https://imgur.com/5GZ8emO.png)

2.  Add plugin to your project

```
cordova plugin add https://github.com/gordeev1/mPOS
```

# Supported Platforms

-   Android

# API

## Methods

-   [mPOS.activateManagerAndGetAvailableDevices](#activateManagerAndGetAvailableDevices)
-   [mPOS.connect](#connect)
-   [mPOS.disconnect](#disconnect)
-   [mPOS.isHaveConnectedReader](#isHaveConnectedReader)
-   [mPOS.payment](#payment)
-   [mPOS.getPaymentStatus](#getPaymentStatus)
-   [mPOS.getReaderInfo](#getReaderInfo)
-   [mPOS.closeDayAndGetReport](#closeDayAndGetReport)

### activateManagerAndGetAvailableDevices

Returns POS devices which paired with a phone

```javascript
mPOS.activateManagerAndGetAvailableDevices('ICMP').then(devices => console.log(devices));
```

```javascript
[
	{
		name: 'iCMP-21400611',
		address: '54:E1:40:FE:96:C8',
		id: '54:E1:40:FE:96:C8 - iCMP-21400611'
	},
	{
		name: 'iCMP-21378793',
		address: '54:E1:40:FE:09:1E',
		id: '54:E1:40:FE:09:1E - iCMP-21378793'
	}
];
```

### connect

```javascript
mPOS.connect('54:E1:40:FE:09:1E - iCMP-21378793').then(() => {});
```

### disconnect

```javascript
mPOS.disconnect().then(() => {});
```

### isHaveConnectedReader

```typescript
mPOS.isHaveConnectedReader().then((have: boolean) => console.log(have));
```

### payment

```javascript
mPOS.payment('7500').then(paymentInfo => console.log(paymentInfo));
```

```javascript
{
	transactionId: "d5304dc9-1e33-4cbb-9d9f-00e5d8d4546f",
	authorizationCode: "69V910"
	cardEntryTypeCode: "NFC"
	cardEntryTypeName: "CONTACTLESS_EMV"
	cardExpiryDate: "08/18"
	cardNumberHash: null
	isPinEntered: false
	isSign: false
	maskedCardNumber: "417398******8943"
	operationDateTime: "Mon Jul 09 20:19:29 GMT+03:00 2018"
	paymentSystemName: "VISA",
	requestId: "0"
	resultCode: "0"
	rrn: "153115677334"
	terminalNumber: "00569988"
	transactionNumber: "0003",
	receipt: `
		09.07.18     20:19    ЧЕК   0003
					Оплата
		Терминал:               00569988
		Мерчант:            564444445555
		Visa              A0000000031010
		Карта:(E1)      ************8943
		Клиент:                        /
		Сумма (Руб):
				75.00
		Комиссия за операцию - 0 Руб.
					ОДОБРЕНО
		Код авторизации:          69V910
		Номер ссылки:       153115677334
		Подпись клиента не требуется  
		1BB95856322072B8FD112A3105EE0801
		================================
	`
}
```

### getPaymentStatus

```javascript
mPOS.getPaymentStatus('d5304dc9-1e33-4cbb-9d9f-00e5d8d4546f').then(paymentInfo =>
	console.log(paymentInfo)
);
```

```
{

}
```

### getReaderInfo

```javascript
mPOS.getReaderInfo().then(info => console.log(info));
```

```javascript
{
	ksn: null;
	readerVersion: '00002267';
	serialNumber: '21378793';
}
```

### closeDayAndGetReport

```javascript
mPOS.closeDayAndGetReport().then(report => console.log(report));
```

```javascript
{
	resultCode: '0';
	terminalNumber: '00569988';
	receipt: `
		09.07.18                   20:23
				Сверка итогов          
		Терминал:               00569988
		Мерчант:            564444445555
		--------------------------------
		Итоги совпали                   
		--------------------------------
		
		Валюта   :                   Руб
		
		Оплата               
		
		09.07.18 20:19       0003
		Kарта:   417398******8943(E1)
		Тип карты:           Visa
		Код авторизации:   69V910
		Сумма:              75.00

		Всего операций:           1
		на сумму:           75.00
		Скидка:              0.00
		--------------------------------
		*******  Отчет закончен  *******
		================================
	`;
}
```

# TODO:

-   add mPOSDisconnected event

# Credits:

Artem Gordeev - [@gordeev1](https://github.com/Gordeev1)
