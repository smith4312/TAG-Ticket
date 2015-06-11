/******************************************************************************
 *
 *		addAgent: Add New Function for TAG AGENT
 *
 ******************************************************************************/

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
	--Can't create a duplicate	
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

/*
 * 	EXAMPLE:
     	select addAgent( 'David', 'Smith', 'DSMITH', 'Password', '123 Main. St.', 'Lakewood', 'NJ', '08701', 'USA',
		'abcdef@gmail.com', '(732) 123-4567', '(732) 123-4567', 'A comment goes here. No loshon Hara', 1 );
*/


/******************************************************************************
 *
 *		updateAgent: Update Function for TAG AGENT
 *
 ******************************************************************************/
DROP FUNCTION IF EXISTS updateAgent (	agentId integer,
				new_first_name varchar(30),
				new_last_name varchar(30),
				new_user_name varchar(30),
				new_password varchar(30),
				new_address varchar(30),
				new_city varchar(30),
				new_state_cd varchar(6),
				new_zip  varchar(10),
				new_country varchar(20),
				new_email varchar(60),
				new_phone  varchar(20),
				new_mobile_phone varchar(20),
				new_notes varchar(255),
				new_office_location_id integer );


CREATE OR REPLACE FUNCTION updateAgent(	agentId integer,
				new_first_name varchar(30),
				new_last_name varchar(30),
				new_user_name varchar(30),
				new_password varchar(30),
				new_address varchar(30),
				new_city varchar(30),
				new_state_cd varchar(6),
				new_zip  varchar(10),
				new_country varchar(20),
				new_email varchar(60),
				new_phone  varchar(20),
				new_mobile_phone varchar(20),
				new_notes varchar(255),
				new_office_location_id integer ) RETURNS text AS $$
BEGIN

	IF NOT EXISTS( SELECT 1 FROM AGENT WHERE id = agentId) THEN
		RETURN 'Agent not found';
	ELSE 
	UPDATE AGENT SET 	first_name = new_first_name,
				last_name = new_last_name,
				user_name = new_user_name,
				password = new_password,
				address = new_address,
				city	= new_city,
				state_cd = new_state_cd,
				zip = new_zip,
				country = new_country,
				email = new_email,
				phone = new_phone,
				mobile_phone = new_mobile_phone,
				notes = new_notes,
				office_location_id = new_office_location_id
	WHERE id = agentId;
	RETURN 'Success'; 
	END IF;
END;
$$ LANGUAGE plpgsql;		

/******************************************************************************
 *
 *		validateAgent: Validate the login credentials for an agent
 *
 ******************************************************************************/

DROP FUNCTION IF EXISTS validateAgent(varchar(30), varchar(30));

CREATE OR REPLACE FUNCTION validateAgent( userName varchar(30), pswd varchar(30)) RETURNS boolean AS $$
BEGIN
	IF EXISTS( SELECT 1 FROM AGENT WHERE user_name =userName AND password = pswd ) THEN
		RETURN true;
	ELSE
		RETURN false;
END IF;
END;
$$ LANGUAGE plpgsql;
