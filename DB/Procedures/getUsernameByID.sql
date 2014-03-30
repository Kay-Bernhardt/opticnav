/* *********************************************************************
**	Description:
**
**  Returns:
********************************************************************* */

DROP FUNCTION IF EXISTS getUsernameByID;

DELIMITER //

CREATE FUNCTION getUsernameByID 
(p_acc_id INT(4))
RETURNS VARCHAR(25)
READS SQL DATA
BEGIN    
    RETURN (SELECT username
            FROM WEB_ACCOUNT
            WHERE p_acc_id = web_account_id);
END//

DELIMITER ;