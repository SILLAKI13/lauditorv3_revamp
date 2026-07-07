package com.digicoffer.lauditor.CommonFiles.PdfUtils;

public class DoctypeUtils {

    public static String docTypes = "{'Argentina': {'driver_license': 'Driver Licence','nic': 'National Identity Document (DNI)','passport': 'Passport'}," +
            "'Australia': {'driver_license': 'Driver Licence','tfn':'Tax File Number (TFN)','passport': 'Passport'}," +
            "'Austria': {'driver_license': 'Driver Licence','nic': 'National Identity Card (CCR ID)','passport': 'Passport'}," +
            "'Belgium': {'driver_license': 'Driver Licence','nic': 'National Register Number (National ID)','passport': 'Passport'}," +
            "'Bosnia': {'driver_license': 'Driver Licence','nic': 'National Identity Card (Citizen Number)','passport': 'Passport'}," +
            "'Brazil': {'cpf':'Cadastro de Pessoas Físicas (CPF)','driver_license': 'Driver Licence','nic': 'National Identity Card','passport': 'Passport','ssc': 'Social Security Card'}," +
            "'Bulgaria': {'driver_license': 'Driver Licence','nic': 'National Identity Card (Civil Number)','passport': 'Passport'}," +
            "'Canada': {'driver_license': 'Driver Licence','sic':'Social Insurance Card','passport': 'Passport'}," +
            "'Chile': {'driver_license': 'Driver Licence','nic': 'National Identity Card (RUN / RUT)','passport': 'Passport'}," +
            "'China': {'driver_license': 'Driver Licence','nic': 'National Identity Card (ID Number)','passport': 'Passport'}," +
            "'Croatia': {'driver_license': 'Driver Licence','nic': 'National Identity Card (Personal Identification Number - OIB)','passport': 'Passport'}," +
            "'Czech Republic': {'birth_number':'Birth Number','driver_license': 'Driver Licence','nic': 'National Identity Card (Citizen Identification Card Number)','passport': 'Passport'}," +
            "'Denmark': {'cvr_number':'CVR Number','driver_license': 'Driver Licence','nic': 'National Identity Card (Personal Identification - CPR Number)','passport': 'Passport','vat_number':'VAT Registration Number'}," +
            "'Estonia': {'driver_license': 'Driver Licence','nic': 'National Identity Card (Personal Identification Code / Number)','passport': 'Passport'}," +
            "'Finland': {'driver_license': 'Driver Licence','hetu':'Personal Identification Number (HETU)','nic': 'National Identity Card','passport': 'Passport'}," +
            "'France': {'driver_license': 'Driver Licence','nic': 'National Identity Card / Number','passport': 'Passport'}," +
            "'Herzegovina': {'driver_license': 'Driver Licence','nic': 'National Identity Card (Citizen Number)','passport': 'Passport'}," +
            "'Iceland': {'driver_license': 'Driver Licence','nic': 'National Identity Card (Kennitala)','passport': 'Passport'}," +
            "'India': {'aadhar': 'AADHAR Card','driver_license': 'Driver Licence','pancard': 'PAN Card','passport': 'Passport','voterid': 'Voter ID'}," +
            "'Indonesia':{'driver_license': 'Driver Licence','nic': 'National Identity Card (National ID)','passport': 'Passport'}," +
            "'Israel': {'driver_license': 'Driver Licence','nic': 'National Identity Card (National ID)','passport': 'Passport'}," +
            "'Italy': {'driver_license': 'Driver Licence','hic':'Health Insurance Card','nic': 'National Identity Card (Fiscal Code Card)','passport': 'Passport'}," +
            "'Latvia': {'driver_license': 'Driver Licence','nic': 'National Identity Card (Personal Code)','passport': 'Passport'}," +
            "'Lithuania': {'driver_license': 'Driver Licence','nic': 'National Identity Card (Personal Code)','passport': 'Passport'}," +
            "'Macedonia': {'driver_license': 'Driver Licence','nic': 'National Identity Card (Citizen Number)','passport': 'Passport'}," +
            "'Malaysia': {'driver_license': 'Driver Licence','nic': 'National Identity Card (National ID)','passport': 'Passport'}," +
            "'Mexico': {'driver_license': 'Driver Licence','nic': 'National Identity Card (CURP) ','passport': 'Passport','ssc': 'Social Security Card'}," +
            "'Moldova': {'driver_license': 'Driver Licence','nic': 'National Identity Card (Personal Code)','passport': 'Passport'}," +
            "'Montenegro': {'driver_license': 'Driver Licence','nic': 'National Identity Card (Citizen Number)','passport': 'Passport'}," +
            "'Netherlands': {'driver_license': 'Driver Licence','nic': 'National Identity Card (Citizen Service / Personal Number)','passport': 'Passport'}," +
            "'New Zealand': {'driver_license': 'Driver Licence','nhi':'National Health Index Number','passport': 'Passport'}," +
            "'Norway': {'driver_license': 'Driver Licence','nic': 'National Identity Card (Birth Number)','passport': 'Passport'}," +
            "'Poland': {'driver_license': 'Driver Licence','nic': 'National Identity Card (Polish National ID Card)','tin':'Tax Identification Number (NIP)','passport': 'Passport','pesel_number':'PESEL Number','regon':'REGON - Identification for Business'}," +
            "'Portugal': {'driver_license': 'Driver Licence','hun':'Health User Number','ic_number':'Civil Identification Number','passport': 'Passport','ssc': 'Social Security Card /  Number','tin':'Tax Identification Number','voterid': 'Voter ID / Number'}," +
            "'Romania': {'driver_license': 'Driver Licence','nic': 'National Identity Card / Personal Numeric Code - CNP','passport': 'Passport'}," +
            "'Serbia': {'driver_license': 'Driver Licence','nic': 'National Identity Card (Citizen Number)','passport': 'Passport'}," +
            "'Singapore': {'driver_license': 'Driver Licence','nic': 'National Identity Card (National ID)','passport': 'Passport'}," +
            "'Slovakia': {'birth_number':'Birth Number','driver_license': 'Driver Licence','nic': 'National Identity Card (Citizen Identification Card Number)','passport': 'Passport'}," +
            "'Slovenia': {'driver_license': 'Driver Licence','nic': 'National Identity Card (Citizen Number)','passport': 'Passport'}," +
            "'South Africa': {'driver_license': 'Driver Licence','nic': 'National Identity Card','passport': 'Passport'}," +
            "'South Korea': {'driver_license': 'Driver Licence','nic': 'National Identity Card (Resident Registration Number)','passport': 'Passport'}," +
            "'Spain': {'driver_license': 'Driver Licence','nic': 'National Identity Card (DNI)','nie': 'NIE','passport': 'Passport'}," +
            "'Sweden': {'driver_license': 'Driver Licence','con': 'Co-Ordination Number','orn':'Organisation Number','passport': 'Passport','nic':'National Identity Card (Personal Identity Number - PIN)'}," +
            "'Switzerland': {'driver_license': 'Driver Licence','passport': 'Passport','ssc': 'Social Security Card'}," +
            "'Turkey': {'driver_license': 'Driver Licence','passport': 'Passport','nic': 'National Identity Card (Turkish Identification Number)'}," +
            "'Ukraine': {'driver_license': 'Driver Licence','passport': 'Passport','nic': 'National Identity Card (Individual Identification Number)'}," +
            "'United Kingdom': {'nino':'National Insurance Number','nhsn':'National Health Service Number','driver_license': 'Driver Licence','passport': 'Passport','ssc': 'Social Security Card','nic':'National Identity Card (Identity Card)'}," +
            "'USA':{'driver_license': 'Driver Licence','passport': 'Passport','ssc': 'Social Security Card'}" +
            "}";
}
