/* *********************************************************************
**	Description:	Will search for the provided account name
**  Returns:        0 if the account name is not found
**                  The account id if the account name is found
********************************************************************* */


DROP FUNCTION IF EXISTS findAccount;

DELIMITER //

CREATE FUNCTION findAccount 
(p_accountName VARCHAR(25))
RETURNS INT
READS SQL DATA
BEGIN
    DECLARE id INT;
    SET id = 0;

    SELECT web_account_id INTO id
    FROM WEB_ACCOUNT
    WHERE user = p_accountName;   

    RETURN id;
END//

DELIMITER ;