import com.github.pagehelper.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xwpf.converter.pdf.PdfConverter;
import org.apache.poi.xwpf.converter.pdf.PdfOptions;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.joda.time.DateTime;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

/*
 * @Description:  文件工具类
 * @Author: 张亚辉 zyh1410@gmail.com
 * @Date: 2019/11/12
 */
@Slf4j
public class FileUtil {

    /**  将InputStream转化为base64
    * @Description:
    * @Param: [in]
    * @return: java.lang.String
    * @Author: 张亚辉
    * @Date: 2019/7/30
    */
    public static String getBase64FromInputStream(InputStream in) {
        byte[] data = null;
        try {
            ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
            byte[] buff = new byte[100];
            int rc = 0;
            while ((rc = in.read(buff, 0, 100)) > 0) {
                swapStream.write(buff, 0, rc);
            }
            data = swapStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return Base64.getEncoder().encodeToString(data);
    }

    /** 
    * @Description: 将文件转为base64字符集 
    * @Param: [file] 
    * @return: java.lang.String 
    * @Author: 张亚辉
    * @Date: 2019/8/2 
    */ 
    public static String encodeBase64File(File file){
        if (file == null) {
            return null;
        }
        try {
            byte[] b = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
            return Base64.getEncoder().encodeToString(b);
        } catch (IOException e) {
            log.error("文件转base64字符集失败！{}",file.getAbsoluteFile());
            log.error(e.getMessage());
        }
        return null;
    }

    /**
     * @Description: 将base64字符集转为文件
     * @Param: [base64Code, targetPath]
     * @return: File
     * @Author: 张亚辉
     * @Date: 2019/7/29
     */
    public static File decoderBase64File(String base64Code,String fileName){
        if (base64Code == null && fileName == null) {
            return null;
        }
        String targetFile = getFilePath(fileName);
        if(StringUtil.isNotEmpty(targetFile)){
            try {
                Files.write(Paths.get(targetFile), Base64.getDecoder().decode(base64Code), StandardOpenOption.CREATE);
            } catch (IOException e) {
                log.error("base64字符集转文件 失败！{}",fileName);
                log.error(e.getMessage());
                return null;
            }
        }else
            return null;
        return new File(targetFile);
    }

    /**
    * @Description:  获取文件存放路径，文件路径为项目根目录/temp/nowTime/文件名
    * @Param: [fileName]
    * @return: java.lang.String
    * @Author: 张亚辉
    * @Date: 2019/7/30
    */
    private static String getFilePath(String fileName){
        String nowTime = DateTime.now().toString("yyyyMMddHHmmssSSS");
        String filePath = System.getProperty("user.dir").replaceAll("\\\\", "/");
        String targetPath = filePath+File.separator+"temp"+File.separator+nowTime;
        File file = new File(targetPath);
        String targetFile = targetPath+File.separator+fileName;
        if(!file.exists()){
            file.mkdirs();
            File file2 = new File(targetFile);
            try {
                file2.createNewFile();
            }catch (IOException i){
                log.error("创建新文件失败！");
                return null;
            }
        }
        return targetFile;
    }

    /**
    * @Description: 将指定文件切割为指定大小的多个文件
    * @Param: [file, bates]  需要切割的文件
    * @return: java.util.List<java.io.File> 切割后每个文件的大小，单位是字节byte
    * @Author: 张亚辉
    * @Date: 2019/7/31
    */
    public static List<File> splitDemoByFile(File file, int bates){
        if(null == file)
            return null;
        String filePath = file.getAbsolutePath();
        List<File> fileList = splitDemoByPath(filePath,bates);
        return fileList;
    }

    /**
     * 根据文件所在路径切割文件
     * @param path 文件的路径
     * @param bates 每个文件的大小(字节byte)
     * @Author: 张亚辉
     * @Date: 2019/7/31
     */
    public static List<File> splitDemoByPath(String path, int bates){
        String parentDir = new File(path).getParent();
        FileOutputStream fos = null;//要在循环内部创建FileOutputStream对象
        FileInputStream fis = null;
        List<File> fileList = new ArrayList<>();
        try {
            fis = new FileInputStream(path);
            byte[] buf = new byte[bates];//将文件分割成1M大小的碎片
            int len,count = 0;
            while ((len = fis.read(buf)) != -1) {
                count+=1;
                String countFlag = "000"+count;
                if(countFlag.length()>4)
                    countFlag = countFlag.substring(countFlag.length()-4);
                String targetPath =  parentDir+File.separator+"Part" + countFlag + ".bin";
                //String targetPath = parentDir+File.separator + "." + count + ".bin";
                fos = new FileOutputStream(targetPath);
                fos.write(buf, 0, len);
                fos.flush();
                fos.close();
                fileList.add(new File(targetPath));
            }
        }catch (Exception e){
            log.error("文件切割失败！{}",path);
            log.error(e.getMessage());
            return null;
        }finally {
            try {
                if (null != fis)
                    fis.close();
            }catch (IOException i){
                log.warn("文件切割流关闭异常!{}",path);
            }
        }
        return fileList;
    }

    /***
    * @Description: 将传入的多个文件合并为一个文件
    * @Param: [fileList, newPath]
    * @return: java.io.File
    * @Author: 张亚辉
    * @Date: 2019/7/31
    */
    public static File sequenceDemoByFileList(List<File> fileList, String newPath){
        FileOutputStream fos = null;
        SequenceInputStream sis = null;
        try {
            fos = new FileOutputStream(FileUtil.mkdirFiles(newPath));
            ArrayList<FileInputStream> al = new ArrayList<FileInputStream>();//Vector效率低
            int count = 0;
            for (int i = 0; i < fileList.size(); i++) {
                al.add(new FileInputStream(fileList.get(i)));
            }
            final Iterator<FileInputStream> it = al.iterator();//ArrayList本身没有枚举方法，通过迭代器来实现
            Enumeration<FileInputStream> en = new Enumeration<FileInputStream>()//匿名内部类，复写枚举接口下的两个方法
            {
                public boolean hasMoreElements() {
                    return it.hasNext();
                }

                public FileInputStream nextElement() {
                    return it.next();
                }

            };
            sis = new SequenceInputStream(en);
            byte[] buf = new byte[1024 * 1024]; //定义1M的缓存区
            while ((count = sis.read(buf)) != -1) {
                fos.write(buf, 0, count);
            }
        }catch (Exception e){
            log.error("文件合并失败！");
            log.error(e.getMessage());
            return null;
        }finally {
            try{
                if(null!=sis)
                    sis.close();
                if(null!=fos)
                    fos.close();
            }catch (IOException i){
                log.warn("文件合并流关闭异常！");
            }
        }
        return new File(newPath);
    }

    /**
     * 根据文件的指定目录和分割文件的后缀合并文件
     * @param path 拆分后的文件所在的目录
     * @param newPath 要合并成哪个目录下的哪个文件，需指定合并后文件的名称及后缀，且保证该目录存在
     * @param suffix 要合并的文件的后缀
     * @Author: 张亚辉
     * @Date: 2019/7/31
     */
    public static File sequenceDemoByPath(String path, String newPath,String suffix){
        FileOutputStream fos = null;
        SequenceInputStream sis = null;
        try {
            fos = new FileOutputStream(newPath);
            ArrayList<FileInputStream> al = new ArrayList<FileInputStream>();//Vector效率低
            int count = 0;
            ArrayList<File> files = getDirFiles(path, suffix);
            for (int x = 0; x < files.size(); x++) {
                al.add(new FileInputStream(files.get(x)));
            }
            final Iterator<FileInputStream> it = al.iterator();//ArrayList本身没有枚举方法，通过迭代器来实现
            Enumeration<FileInputStream> en = new Enumeration<FileInputStream>()//匿名内部类，复写枚举接口下的两个方法
            {
                public boolean hasMoreElements() {
                    return it.hasNext();
                }

                public FileInputStream nextElement() {
                    return it.next();
                }

            };
            sis = new SequenceInputStream(en);
            byte[] buf = new byte[1024 * 1024]; //定义1M的缓存区
            while ((count = sis.read(buf)) != -1) {
                fos.write(buf, 0, count);
            }
        }catch (Exception e){
            log.error("文件合并失败！路径为：{}，后缀为：{}",path,suffix);
            log.error(e.getMessage());
            return null;
        }finally {
            try{
                if(null!=sis)
                    sis.close();
                if(null!=fos)
                    fos.close();
            }catch (IOException i){
                log.warn("文件合并流关闭异常！");
            }
        }
        return new File(newPath);
    }

    /**
     * 获取路径内所有指定后缀的文件
     * @param dirPath
     * @param suffix
     * @return
     */
    public static ArrayList<File> getDirFiles(String dirPath,
                                              final String suffix) {
        File path = new File(dirPath);
        File[] fileArr = path.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                String lowerName = name.toLowerCase();
                String lowerSuffix = suffix.toLowerCase();
                if (lowerName.endsWith(lowerSuffix)) {
                    return true;
                }
                return false;
            }

        });
        ArrayList<File> files = new ArrayList<File>();

        for (File f : fileArr) {
            if (f.isFile()) {
                files.add(f);
            }
        }
        return files;
    }

    /**
     * 创建文件并检测生成相应父级目录
     * @param path
     * @return
     */
    public static File mkdirFiles(String path) {
        File file = new File(path);
        try {
            if (!file.getParentFile().exists() && !file.isDirectory()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } else {
                file.createNewFile();
            }
        } catch (IOException e) {
            log.error("文件生成有误：\n"+e);
        }
        return file;
    }
    
    /**
     * 创建文件并检测生成相应父级目录
     * @param path
     * @return
     */
    public static void mkdirFolder(String path) {
        File file = new File(path);
        if (!file.exists() && !file.isDirectory()) {
		    file.mkdirs();
		}
    }


    /**
     *
     * 删除单个文件
     * @param filePath 文件地址
     */
    public static void delFile(String filePath) {
        try {
            filePath = filePath.toString();
            File myFilePath = new File(filePath);
            myFilePath.delete(); //删除空文件夹
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除文件夹以及其中所有文件
     * @param folderPath
     */
    public static void delFolder(String folderPath) {
        try {
            delAllFile(folderPath); //删除完里面所有内容
            String filePath = folderPath;
            filePath = filePath.toString();
            File myFilePath = new File(filePath);
            myFilePath.delete(); //删除空文件夹
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除路径下所有文件
     * @param path
     * @return
     */
    public static boolean delAllFile(String path) {
        boolean flag = false;
        File file = new File(path);
        if (!file.exists()) {
            return flag;
        }
        if (!file.isDirectory()) {
            return flag;
        }
        String[] tempList = file.list();
        File temp = null;
        for (int i = 0; i < tempList.length; i++) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + tempList[i]);
            } else {
                temp = new File(path + File.separator + tempList[i]);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                delAllFile(path + "/" + tempList[i]);//先删除文件夹里面的文件
                delFolder(path + "/" + tempList[i]);//再删除空文件夹
                flag = true;
            }
        }
        return flag;
    }

    public static List<String> readFile(String path){
        File file = new File(path);
        return  readFile(file);
    }

    public static List<String> readFile(File file){
        List<String> result=new ArrayList<>();
        try {

            if(!file.exists()){
                return new ArrayList<>();
            }
            //读取文件(字符流)
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF-8"));
            //读取数据
            //循环取出数据
            String str = null;
            while ((str = in.readLine()) != null) {
                result.add(str);
            }
            //关闭流
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
            result=new ArrayList<>();
        }
        return result;
    }

    public static void writeFile(String filePath,List<String> lines,boolean iswriting){
        try {
            BufferedWriter out =getBufferedWriter(filePath,iswriting);
            for (String line:lines) {
                out.write(line);
                out.newLine();
            }
            //清楚缓存
            out.flush();
            //关闭流
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeFile(String filePath,String line,boolean iswriting){
        try {
            BufferedWriter out=getBufferedWriter(filePath,iswriting);
            out.write(line);
            out.newLine();
            //清楚缓存
            out.flush();
            //关闭流
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static BufferedWriter getBufferedWriter(String filePath,boolean iswriting){
        BufferedWriter out=null;
        try {
            File file = new File(filePath);
            FileOutputStream fos = null;
            if(!file.exists()){
                file=FileUtil.mkdirFiles(filePath);
                fos = new FileOutputStream(file);//首次写入获取
            }else{
                fos = new FileOutputStream(file,iswriting);//这里构造方法多了一个参数true,表示在文件末尾追加写入
            }
            //写入相应的文件
            out= new BufferedWriter(new OutputStreamWriter(fos,"UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out;
    }

    /**
     * 将docx文件转为pdf
     * @param docxStream 要转换的文件的流
     * @return
     * @throws Exception
     */
    public static byte[] docxToPdf(InputStream docxStream) throws Exception {
        ByteArrayOutputStream targetStream = null;
        XWPFDocument doc = null;
        try {
            doc = new XWPFDocument(docxStream);
            PdfOptions options = PdfOptions.create();
            // 中文字体处理
            /*options.fontProvider(new IFontProvider() {

                @Override
                public Font getFont(String familyName, String encoding, float size, int style, java.awt.Color color) {
                    try {
                        BaseFont bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
                        Font fontChinese = new Font(bfChinese, 12, style, color);
                        if (familyName != null)
                            fontChinese.setFamily(familyName);
                        return fontChinese;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            });*/

            targetStream = new ByteArrayOutputStream();
            PdfConverter.getInstance().convert(doc, targetStream, options);
            return targetStream.toByteArray();
        } catch (IOException e) {
            throw new Exception(e);
        } finally {
            IOUtils.closeQuietly(targetStream);
        }
    }

}
