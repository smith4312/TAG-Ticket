/*******************************************************************************
 *
 *	OFFICE_LOCATION
 *
 *******************************************************************************/
INSERT INTO OFFICE_LOCATION VALUES(DEFAULT, 'Lakewood', 'Lakewood TAG Office', '1221 Madison Ave.',
	'Lakewood', 'NJ', '08701', 'USA', '732-730-1824', '', '', 'Monday - Thursday\n9:30 am - 11:45 pm\nFriday\n9:00 am - 1:00 pm\nSunday\n10:00 am - 11:45 pm' 	);
INSERT INTO OFFICE_LOCATION VALUES(DEFAULT, 'Boro Park', 'Boro Park', '5316 New Utrecht Ave',
	'Brooklyn', 'NY', '11219', 'USA', '732-730-1824', '', '', 'Monday - Thursday\n11:00 am - 6:00 pm\nSunday\n11:00 pm - 5:00 pm' 	);
INSERT INTO OFFICE_LOCATION VALUES(DEFAULT, 'Flatbush', 'TAG Flatbush', '1622 Coney Island Ave',
	'Brooklyn', 'NY', '11230', 'USA', '732-730-1824', '', '', 'Monday - Thursday\n10:00 am - 4:00 pm\nFriday\n9:15 am - 11:15 am\nSunday\n9:15 am - 4:00 pm' 	);
INSERT INTO OFFICE_LOCATION VALUES(DEFAULT, 'Monsey', 'TAG Monsey', '19 Main St.',
	'Monsey', 'NY', '10952', 'USA', '732-730-1824', '', '', 'Sunday - Thursday\n11:30 am - 6:00 pm\nThursday Evening\n8:00 - 10:00 pm' 	);
INSERT INTO OFFICE_LOCATION VALUES(DEFAULT, 'Williamsburg', 'Williamsburg TAG Office', '14 Lee Ave.',
	'Williamsburg', 'NY', '11211', 'USA', '732-730-1824', '', '', 'Sunday - Thursday\n1:30 pm - 3:30 pm\n8:30pm - 10:30pm' 	);


/*******************************************************************************
 *
 *	TICKET_STATUS
 *
 *******************************************************************************/
INSERT INTO TICKET_STATUS VALUES (DEFAULT, 'New', 'New unassigned ticket' );
INSERT INTO TICKET_STATUS VALUES (DEFAULT, 'Open', 'Assigned ticket' );
INSERT INTO TICKET_STATUS VALUES (DEFAULT, 'Drop Off', 'A drop off that not assigned' );
INSERT INTO TICKET_STATUS VALUES (DEFAULT, 'Pick Up', 'Ready for customer pick up' );
INSERT INTO TICKET_STATUS VALUES (DEFAULT, 'Closed', 'Completed Ticket' );
INSERT INTO TICKET_STATUS VALUES (DEFAULT, 'Cancelled', 'Cancelled Ticket' );


/*******************************************************************************
 *
 *	TICKET_ACTION
 *
 *******************************************************************************/
INSERT INTO TICKET_ACTION VALUES (DEFAULT, 'Install Filter', 	'Filters', 'Install a new filter on a device', '', 'Check if payment is required');
INSERT INTO TICKET_ACTION VALUES (DEFAULT, 'Adjust Filter',  	'Filters', 'Install a new filter on a device', '', '');
INSERT INTO TICKET_ACTION VALUES (DEFAULT, 'App Lock', 		 	'Protection', 'Install a new filter on a device', '', '');
INSERT INTO TICKET_ACTION VALUES (DEFAULT, 'Block Texting', 	'Protection', 'Install a new filter on a device', '', '');
INSERT INTO TICKET_ACTION VALUES (DEFAULT, 'Block Texting and Web', 'Protection', 'Install a new filter on a device', '', '');
INSERT INTO TICKET_ACTION VALUES (DEFAULT, 'Chinuch', 			'Education', 'Install a new filter on a device', '', '');
INSERT INTO TICKET_ACTION VALUES (DEFAULT, 'Fix', 				'Other Service', 'Install a new filter on a device', '', '');
INSERT INTO TICKET_ACTION VALUES (DEFAULT, 'Information', 		'Education', 'Install a new filter on a device', '', '');
INSERT INTO TICKET_ACTION VALUES (DEFAULT, 'Pick Up', 			'Filters', 'Install a new filter on a device', '', '');
INSERT INTO TICKET_ACTION VALUES (DEFAULT, 'Remove Internet', 	'Protection', 'Install a new filter on a device', '', '');
INSERT INTO TICKET_ACTION VALUES (DEFAULT, 'Restrict Internet', 'Protection', 'Install a new filter on a device', '', '');
INSERT INTO TICKET_ACTION VALUES (DEFAULT, 'Update', 			'Protection', 'Install a new filter on a device', '', '');
INSERT INTO TICKET_ACTION VALUES (DEFAULT, 'Remove Video', 		'Protection', 'Install a new filter on a device', '', '');
INSERT INTO TICKET_ACTION VALUES (DEFAULT, 'Remove Wifi and Video', 'Protection', 'Install a new filter on a device', '', '');


/*******************************************************************************
 *
 *	AGENT
 *
 *******************************************************************************/
INSERT INTO AGENT VALUES (DEFAULT, 'David', 'Smith', 'DSMITH', 'Password', '123 Main. St.', 'Lakewood', 'NJ', '08701', 'USA',
	'abcdef@gmail.com', '(732) 123-4567', '(732) 123-4567', 'A comment goes here. No loshon Hara', 1 );
INSERT INTO AGENT VALUES (DEFAULT, 'Yanki', 'Weinberg', 'YWEINBERG', 'Password', '123 Main. St.', 'Lakewood', 'NJ', '08701', 'USA',
	'abcdef@gmail.com', '(732) 123-4567', '(732) 123-4567', 'A comment goes here. No loshon Hara', 1 );
INSERT INTO AGENT VALUES (DEFAULT, 'Tzvi', 'Bock', 'TBOCK', 'Password', '123 Main. St.', 'Lakewood', 'NJ', '08701', 'USA',
	'abcdef@gmail.com', '(732) 123-4567', '(732) 123-4567', 'A comment goes here. No loshon Hara', 1 );
INSERT INTO AGENT VALUES (DEFAULT, 'Shmuel', 'Kipper', 'SKIPPER', 'Password', '123 Main. St.', 'Lakewood', 'NJ', '08701', 'USA',
	'abcdef@gmail.com', '(732) 123-4567', '(732) 123-4567', 'A comment goes here. No loshon Hara', 1 );
INSERT INTO AGENT VALUES (DEFAULT, 'Naftoli', 'Gugenheim', 'NGUGENHEIM', 'Password', '123 Main. St.', 'Lakewood', 'NJ', '08701', 'USA', 
	'abcdef@gmail.com', '(732) 123-4567', '(732) 123-4567', 'A comment goes here. No loshon Hara', 1 );
INSERT INTO AGENT VALUES (DEFAULT, 'Isaac', 'Lichtenstein', 'ILICHTENSTEIN', 'Password', '123 Main. St.', 'Lakewood', 'NJ', '08701', 'USA',
	'abcdef@gmail.com', '(732) 123-4567', '(732) 123-4567', 'A comment goes here. No loshon Hara', 1 );		
INSERT INTO AGENT VALUES (DEFAULT, 'Moshe', 'Drew', 'MDREW', 'Password', '123 Main. St.', 'Lakewood', 'NJ', '08701', 'USA',
	'abcdef@gmail.com', '(732) 123-4567', '(732) 123-4567', 'A comment goes here. No loshon Hara', 1 );	
	
	

/*******************************************************************************
 *
 *	ACCOUNT
 *
 *******************************************************************************/
INSERT INTO ACCOUNT VALUES (DEFAULT, 'Benny', 'Friedman', '23 Eastern Pkwy', 'Crown Heights', 'NY', '12356', 'benny@gmail.com',
'(718) 342-2345', '(972) 234-1564',	'Loves Music', 2, 1);

INSERT INTO ACCOUNT VALUES (DEFAULT, 'David', 'Lowy', '18 Ocean Pkwy', 'Brooklyn', 'NY', '12256', 'david@gmail.com',
'(718) 213-2345', '(972) 456-1564',	'Also loves Music', 2, 0);	


/*******************************************************************************
 *
 *	PERSON
 *
 *******************************************************************************/
INSERT INTO PERSON VALUES	(DEFAULT, 'Benny', 'Friedman', 1, 'M', 'M', 'benny@gmail.com',
		'(718) 342-2345', '(972) 234-1564',	'Loves Music');
		
INSERT INTO PERSON VALUES	(DEFAULT, 'Fraidy', 'Friedman', 1, 'F', 'M', 'fraidy@gmail.com',
		'(718) 342-2345', '(972) 123-1564',	'Doesnt like Music');		
		
INSERT INTO PERSON VALUES	(DEFAULT, 'David', 'Lowy', 2, 'M', 'M', 'david@gmail.com',
		'(718) 213-2345', '(972) 456-1564',	'Also loves Music');


/*******************************************************************************
 *
 *	DEVICE
 *
 *******************************************************************************/
INSERT INTO DEVICE VALUES (DEFAULT, 'COMPUTER','DELL', 'INSPIRON', 'WINDOWS 8.1', 'CHROME', 'Requires NativeUSA Filter');
INSERT INTO DEVICE VALUES (DEFAULT, 'COMPUTER', 'APPLE', 'MAC BOOK', 'OSX 10.8', 'Safari',   'Requires NativeUSA Filter');
INSERT INTO DEVICE VALUES (DEFAULT, 'SMART PHONE', 'APPLE', 'iPhone', 'iOS 7.12', 'Safari', 'Requires NativeUSA Filter');


/*******************************************************************************
 *
 *	FILTER
 *
 *******************************************************************************/
INSERT INTO FILTER VALUES (DEFAULT, 'NativeUSA', 'www.usanativ.com/', 'some details go here');
INSERT INTO FILTER VALUES (DEFAULT, 'K9', 'www1.k9webprotection.com/', 'some details go here');


/*******************************************************************************
 *
 *	PERSON_DEVICE
 *
 *******************************************************************************/
INSERT INTO PERSON_DEVICE VALUES(
DEFAULT, 1, 1, 'username', 'password', 1, 'username', 'password', 'ok to open url at users request' );

INSERT INTO PERSON_DEVICE VALUES(
DEFAULT, 2, 1, 'username', 'password', 1, 'username', 'password', 'ok to open url at users request' );

INSERT INTO PERSON_DEVICE VALUES(
DEFAULT, 3, 2, 'username', 'password', 1, 'username', 'password', 'ok to open url at users request' );


/*******************************************************************************
 *
 *	TICKET
 *
 *******************************************************************************/
INSERT INTO TICKET VALUES (DEFAULT, 1, 2, 1, 1, 1, 1, 'Install a new filter', 2, 'no rush', 1, 1 );

