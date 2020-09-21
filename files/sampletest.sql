use CIMS
UPDATE CIMS.Customer.ClaimBlackList SET IsIndividual=(CASE IsIndividual WHEN 1 THEN 0 ELSE 1 END) WHERE CO_ContractID=407
select * from CIMS.Customer.ClaimBlackList WHERE CO_ContractID=407

UPDATE CIMS.Financial.ClaimCostPlus SET CCP_AdminFee=(CASE CCP_AdminFee WHEN 70 THEN 71 ELSE 70 END) WHERE CCP_ID=1
SELECT * FROM CIMS.Financial.ClaimCostPlus WHERE CCP_ID=1
