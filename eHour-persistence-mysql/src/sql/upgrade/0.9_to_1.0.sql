DROP TABLE PROJECT_ASSIGNMENT;
DROP TABLE PROJECT_ASSIGNMENT_TYPE;
ALTER TABLE MAIL_LOG_ASSIGNMENT DROP FOREIGN KEY MAIL_LOG_ASSIGNMENT_fk1 ;
ALTER TABLE MAIL_LOG_ASSIGNMENT DROP COLUMN PROJECT_ASSIGNMENT_ID , DROP INDEX PROJECT_ASSIGNMENT_ID , RENAME TO  MAIL_LOG_ACTIVITY ;
ALTER TABLE MAIL_LOG_ACTIVITY ADD COLUMN ACTIVITY_ID INT NULL;


