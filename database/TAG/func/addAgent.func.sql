
DROP FUNCTION IF EXISTS addAgent (first_name varchar(30),
					last_name varchar(30),
					userName varchar(30),
					password varchar(30),
					address varchar(30),
					city varchar(30),
					state_cd varchar(6),
					zip  varchar(10),
					country varchar(20),
					email varchar(60),
					phone  varchar(20),
					mobile_phone varchar(20),
					notes varchar(255),
					office_location_id integer );


CREATE OR REPLACE FUNCTION addAgent(	first_name varchar(30),
					last_name varchar(30),
					userName varchar(30),
					password varchar(30),
					address varchar(30),
					city varchar(30),
					state_cd varchar(6),
					zip  varchar(10),
					country varchar(20),
					email varchar(60),
					phone  varchar(20),
					mobile_phone varchar(20),
					notes varchar(255),
					office_location_id integer ) RETURNS text AS $$
BEGIN
	--validation
	--if username is taken

	IF userName = '' THEN
		RETURN 'User name must be at least 4 characters';
	ELSIF EXISTS( SELECT 1 FROM AGENT WHERE user_name = userName) THEN
		RETURN 'User Name Already Exists';
	ELSE 
	INSERT INTO AGENT VALUES (DEFAULT, first_name, last_name , userName, password, 
			address, city, state_cd, zip, country, email, phone, mobile_phone, notes, office_location_id);
	RETURN 'Success';
	END IF;
END;
$$ LANGUAGE plpgsql;		


--select addAgent( 'David', 'Smith', 'DSMITH', 'Password', '123 Main. St.', 'Lakewood', 'NJ', '08701', 'USA',
--	'abcdef@gmail.com', '(732) 123-4567', '(732) 123-4567', 'A comment goes here. No loshon Hara', 1 );
