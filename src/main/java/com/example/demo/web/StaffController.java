package com.example.demo.web;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.model.StaffModel;
import com.example.demo.model.util.Result;
import com.example.demo.model.util.ResultGenerator;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.*;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.apache.commons.codec.binary.Base64;

import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @program:yttps_collection_infomation
 * @description:
 * @author: Mr.Henry
 * @create:2019/6/20 14:11
 */

@RestController
@RequestMapping("/staff")
public class StaffController {
    private final static Logger logger = LoggerFactory.getLogger(StaffController.class);
    @Value("${excel_path}")
    private String path;

    private SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd");

    //base64字符串转化成图片
    public String GenerateImage(String imgStr, String fileName) {   //对字节数组字符串进行Base64解码并生成图片
        if (imgStr == null) //图像数据为空
            return null;
        try {
            String baseValue = imgStr.replaceAll(" ", "+");//前台在用Ajax传base64值的时候会把base64中的+换成空格，所以需要替换回来。
            byte[] b = Base64.decodeBase64(baseValue.replace("data:image/jpeg;base64,", ""));//去除base64中无用的部分
            for (int i = 0; i < b.length; ++i) {
                if (b[i] < 0) {//调整异常数据
                    b[i] += 256;
                }
            }
            File dir = new File(path + "/img");
            if (!dir.canExecute()) {
                dir.mkdir();
            }
            //生成jpeg图片
            String imgFilePath = path + "/img/" + fileName + ".jpg";//新生成的图片
            OutputStream out = new FileOutputStream(imgFilePath);
            out.write(b);
            out.flush();
            out.close();
            return "img/"+fileName+ ".jpg";
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void insertDataInSheet(HSSFWorkbook wb, HSSFSheet sheet, List<StaffModel> info) {
        int rowNum = 1;
        //画图的顶级管理器，一个sheet只能获取一个（一定要注意这点）
        HSSFPatriarch patriarch = sheet.createDrawingPatriarch();
        String[] excelHeaders = {"*姓名/Name", "*员工号/Employee Number", "出生日期/Birthday（YYYY-MM-DD)", "入职时间/Hiredate（YYYY-MM-DD", "联系方式/Contact", "*标签/Tags（用\"/\"隔开", "*卡号/Card Number", "*照片文件路径/Photo Path（文件夹/文件名", "备注/Notes"};
        //headers表示excel表中第一行的表头
        HSSFRow row3 = sheet.createRow(0);
        //在excel表中添加表头
        for (int i = 0; i < excelHeaders.length; i++) {
            HSSFCell cell = row3.createCell(i);
            HSSFRichTextString text = new HSSFRichTextString(excelHeaders[i]);
            cell.setCellValue(text);
        }
        //在表中存放查询到的数据放入对应的列
        int index = 1;
        for (StaffModel staff : info) {
            HSSFRow row = sheet.createRow(rowNum);
            row.setHeight((short) 1000);
//                ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
//                String url = PARK_BASE_URL + "/image/" + staff.getFace_list().get(0).getFace_image_id();
//                BufferedImage image = null;
            row.createCell(0).setCellValue(staff.getName());
            row.createCell(1).setCellValue(staff.getStaffId());
            row.createCell(2).setCellValue(staff.getBirthday());
            row.createCell(3);
            row.createCell(4).setCellValue(staff.getPhone());
            row.createCell(5).setCellValue(staff.getTagName());
            row.createCell(6);
            String url = GenerateImage(staff.getBase64Face(), staff.getStaffId());
            row.createCell(7).setCellValue(url);
            row.createCell(8);
            rowNum++;
        }
    }

    @PostMapping("/setStaff")
     public Result setStaff(String info) {
        logger.info("开始文件操作");
        File dir = new File(path);
        if (dir.exists()) {
            File file = new File(path + "/staffInformationList.xlsx");
            if (file.exists()) {
                logger.info("文件以存在,进行修改操作");
                StaffModel staffModel = JSONObject.parseObject(info, StaffModel.class);
                updateExcel(staffModel, file);
            }
        } else {
            dir.mkdir();
            File file = new File(path + "/staffInformationList.xlsx");
            StaffModel staffModel = JSONObject.parseObject(info, StaffModel.class);
            createExcel(staffModel);
        }
        return ResultGenerator.genSuccessResult();
    }

    private void updateExcel(StaffModel staffModel, File file) {
        FileInputStream fis = null;
        Workbook book = null;
        WritableWorkbook bookWrit = null;
        logger.info("开始读取数据");
        try {
            fis = new FileInputStream(file);
            book = Workbook.getWorkbook(fis);
            Sheet[] sheet = book.getSheets();
            Integer cols = null;
            Integer rowIndex = null;
            for (int i = 0; i < sheet.length; i++) {
                Sheet rs = book.getSheet(i);
                //跳过查询第一行
                cols = rs.getRow(0).length;
                for (int j = 1; j < rs.getRows(); j++) {
                    Cell[] cells = rs.getRow(j);
                    if (!cells[1].getContents().equals(staffModel.getStaffId())) {
                        continue;
                    } else {
                        rowIndex = j;
                        break;
                    }
                }
                if (rowIndex == null) {
                    rowIndex = rs.getRows();
                }
            }

            //写入数据
            bookWrit = Workbook.createWorkbook(file, book);
            if (rowIndex != null) {
                logger.info("获取bookWrit中的信息");
                WritableSheet sheet1 = bookWrit.getSheet(0);
                Label label = new Label(0, rowIndex, staffModel.getName());
                Label labe2 = new Label(1, rowIndex, staffModel.getStaffId());
                Label labe3 = new Label(2, rowIndex, staffModel.getBirthday());
                Label labe4 = new Label(3, rowIndex, "");
                Label labe5 = new Label(4, rowIndex, staffModel.getPhone());
                Label labe6 = new Label(5, rowIndex, staffModel.getTagName());
                Label labe7 = new Label(6, rowIndex, "");
                String url = GenerateImage(staffModel.getBase64Face(), staffModel.getStaffId());
                Label labe8 = new Label(7, rowIndex, url);
                sheet1.addCell(label);
                sheet1.addCell(labe2);
                sheet1.addCell(labe3);
                sheet1.addCell(labe4);
                sheet1.addCell(labe5);
                sheet1.addCell(labe6);
                sheet1.addCell(labe7);
                sheet1.addCell(labe8);
                logger.warn("sheet新增一行数据，当前长度为:"+sheet.length);
                bookWrit.write();
            }

        } catch (FileNotFoundException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        } catch (IOException | WriteException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        } catch (BiffException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                fis.close();
                book.close();
                bookWrit.close();
            } catch (IOException | WriteException e) {
                e.printStackTrace();
            }
        }
    }

    private void createExcel(StaffModel staffModel) {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("staff");
        ///设置要导出的文件的名字
        String fileName = "/staffInformationList.xlsx";
        List<StaffModel> staffModelList = new ArrayList<>();
        staffModelList.add(staffModel);
        //新增数据行，并且设置单元格数据
        insertDataInSheet(workbook, sheet, staffModelList);
        try {
            FileOutputStream out = new FileOutputStream(path + fileName);
            workbook.write(out);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取Excel的内容，第一维数组存储的是一行中格列的值，二维数组存储的是多少个行
     *
     * @param file       读取数据的源Excel
     * @param ignoreRows 读取数据忽略的行数，比喻行头不需要读入 忽略的行数为1
     * @return 读出的Excel中数据的内容
     * @throws FileNotFoundException
     * @throws IOException
     */
    private static String[][] getData(File file, int ignoreRows)
            throws FileNotFoundException, IOException {
        List<String[]> result = new ArrayList<String[]>();
        int rowSize = 0;
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(
                file));
        // 打开HSSFWorkbook
        POIFSFileSystem fs = new POIFSFileSystem(in);
        HSSFWorkbook wb = new HSSFWorkbook(fs);
        HSSFCell cell = null;
        for (int sheetIndex = 0; sheetIndex < wb.getNumberOfSheets(); sheetIndex++) {
            HSSFSheet st = wb.getSheetAt(sheetIndex);
            // 第一行为标题，不取
            for (int rowIndex = ignoreRows; rowIndex <= st.getLastRowNum(); rowIndex++) {
                HSSFRow row = st.getRow(rowIndex);
                if (row == null) {
                    continue;
                }
                int tempRowSize = row.getLastCellNum() + 1;
                if (tempRowSize > rowSize) {
                    rowSize = tempRowSize;
                }
                String[] values = new String[rowSize];
                Arrays.fill(values, "");
                boolean hasValue = false;
                for (short columnIndex = 0; columnIndex <= row.getLastCellNum(); columnIndex++) {
                    String value = "";
                    cell = row.getCell(columnIndex);
                    if (cell != null) {
                        // 注意：一定要设成这个，否则可能会出现乱码
                        switch (cell.getCellType()) {
                            case HSSFCell.CELL_TYPE_STRING:
                                value = cell.getStringCellValue();
                                break;
                            case HSSFCell.CELL_TYPE_NUMERIC:
                                if (HSSFDateUtil.isCellDateFormatted(cell)) {
                                    Date date = cell.getDateCellValue();
                                    if (date != null) {
                                        value = new SimpleDateFormat("yyyy-MM-dd")
                                                .format(date);
                                    } else {
                                        value = "";
                                    }
                                } else {
                                    value = new DecimalFormat("0").format(cell
                                            .getNumericCellValue());
                                }
                                break;
                            case HSSFCell.CELL_TYPE_FORMULA:
                                // 导入时如果为公式生成的数据则无值
                                if (!cell.getStringCellValue().equals("")) {
                                    value = cell.getStringCellValue();
                                } else {
                                    value = cell.getNumericCellValue() + "";
                                }
                                break;
                            case HSSFCell.CELL_TYPE_BLANK:
                                break;
                            case HSSFCell.CELL_TYPE_ERROR:
                                value = "";
                                break;
                            case HSSFCell.CELL_TYPE_BOOLEAN:
                                value = (cell.getBooleanCellValue() == true ? "Y"
                                        : "N");
                                break;
                            default:
                                value = "";
                        }
                    }
                    if (columnIndex == 0 && value.trim().equals("")) {
                        break;
                    }
                    values[columnIndex] = rightTrim(value);
                    hasValue = true;
                }
                if (hasValue) {
                    result.add(values);
                }
            }
        }
        in.close();
        String[][] returnArray = new String[result.size()][rowSize];
        for (int i = 0; i < returnArray.length; i++) {
            returnArray[i] = (String[]) result.get(i);
        }
        return returnArray;
    }


    /**
     * 去掉字符串右边的空格
     *
     * @param str 要处理的字符串
     * @return 处理后的字符串
     */

    public static String rightTrim(String str) {
        if (str == null) {
            return "";
        }
        int length = str.length();
        for (int i = length - 1; i >= 0; i--) {
            if (str.charAt(i) != 0x20) {
                break;

            }
            length--;

        }
        return str.substring(0, length);

    }
}
