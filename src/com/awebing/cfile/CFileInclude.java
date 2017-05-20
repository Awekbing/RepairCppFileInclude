package com.awebing.cfile;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

public class CFileInclude {
	
//	Project  ---|  Classes ---| App.cpp   delegate.cpp
//				|  Resource
	
	static Logger log = Logger.getLogger(CFileInclude.class);
	static String PROJECT_DIR = "整个工程的根目录";	
	// 我这里为了方便留一份备份都是复制一份操作的
	static String BAK_DIR = PROJECT_DIR + "\\Classes_Orignal";
	static String CLASS_DIR = PROJECT_DIR + "\\Classes";
	static List<String> files = new ArrayList<>();
	static List<String> filenames = new ArrayList<>(); 
	static int times = 0;
	
	public static void main(String[] args) throws Exception {
		copyToNewDir();
		allFiles(CLASS_DIR);
		System.out.println("文件总数：" + files.size());
		for (String s : files) {
			readInclude(s);
		}
		System.out.println("调用次数:" + times);
	}
	
	private static void copyToNewDir() throws Exception {
		File file = new File(CLASS_DIR);
		if(file.exists()) FileUtils.deleteDirectory(file);
		FileUtils.copyDirectory(new File(BAK_DIR), file);
	}

	static void readInclude(String filepath) throws Exception {
		File file = new File(filepath);
		String readFileToString = FileUtils.readFileToString(file);
		String regex = "#include\\s+\"\\S+.h\"";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(readFileToString);
		while(m.find()) {
			String includeFile = m.group();
			String replaceStr = includeFile;
			// 获取此文件所在目录
			String parent = file.getParent();
			// 分离出文件名称
			int start = includeFile.indexOf("\"");
			int end = includeFile.lastIndexOf("\"");
			// 要查找的文件名称
			includeFile = includeFile.substring(start + 1, end);
			if(includeFile.contains("GameChatLayer")) {
				System.out.println(111);
			}
			// 当前目录索引头文件是否存在  （direction/../../headfile.h）
			if(!isExistsFile(parent, includeFile)) {
				if(isFileInIncludeList(includeFile)) {
					String headFilePath = getHeadFilePath(filepath, parent, includeFile);
					String includeFormat = String.format("#include \"%s\"", headFilePath);
					readFileToString = readFileToString.replaceFirst(replaceStr, includeFormat);
					FileUtils.write(file, readFileToString);
				}
			}
		}
	}
	
	static String getHeadFilePath(String orgFilePath, String curPath, String file) {
		// 当前header.h文件
		File file2 = new File(curPath);		
		FilePath fbr = findFileByRecursion(orgFilePath, curPath, curPath, file);
		if( fbr != null ) {
			String string = fbr.prefix + fbr.name;
			return string;
		} else {
			String parent = file2.getParent();
			if(parent.equals(PROJECT_DIR)) return  "";
			String headFilePath = getHeadFilePath(orgFilePath, parent, file);
			if(headFilePath != null) return headFilePath;
		}
		return "";
	}
	
	/***
	 * 递归查找目录下是否存在文件 
	 * @param dir
	 * @param file
	 * @param pathLevel    等级就是那个 ../../
	 * @param addStr       追加目录名称 
	 * @return
	 */
	static FilePath findFileByRecursion(String orgFilePath, String curPath, String dir, String file) {
		File f = new File(dir);
		File[] listFiles = f.listFiles();
		if(listFiles == null) return null;
		for (File file2 : listFiles) {
			// 如果是文件
			if(!file2.isDirectory()) {
				if(file2.getName().toLowerCase().equals(file.toLowerCase())) {
					FilePath fp = new FilePath();
					List<String> parentWithClasses = getParentStartFromClasses(file2.getParentFile(), null);
					Collections.reverse(parentWithClasses);
					String classPath = "";
					for (String s : parentWithClasses) {
						classPath += s;
					}
					fp.name = classPath + file2.getName();
					int level = getLevel(new File(orgFilePath), 0) - 1;
					String prefix = "";
					for (int i = 0; i < level; i++) {
						prefix += "../";
					}
					fp.prefix = prefix;
					System.out.println("获取父目录：" + parentWithClasses);
					times++;
					return fp;
				}
			} else { 
				// 如果是目录  查找子目录
				FilePath findFileByRecursion = findFileByRecursion(orgFilePath, curPath, file2.getPath(), file);
				if(findFileByRecursion != null) return findFileByRecursion;
			}
		}
		return null;
	}
	
	static int getLevel(File file ,int backDeep) {
		if(file.getName().equals("Classes")) {
			return backDeep;
		}
		backDeep++;
		return getLevel(file.getParentFile(), backDeep);
	}
	
	static ArrayList<String> getParentStartFromClasses(File file, ArrayList<String> list) {
		if(list == null ) list = new ArrayList<>();
		if(file.getName().equals("Classes")) {
			return list;
		}
		list.add(file.getName() + "/");
		return getParentStartFromClasses(file.getParentFile(), list);
	}
	
	static boolean isFileInIncludeList(String includeFile) {
		boolean result = false;
		for (String s : filenames) {
			if(s.toLowerCase().equals(includeFile.toLowerCase())) {
				result = true;
				break;
			}
		}
		return result;
	}
	
	static boolean isExistsFile(String dir, String file) {
		File f = new File(dir + File.separator + file);
		if(f.exists()) return true;
		return false;
	}
	
	static void allFiles(String path) {
		File dir = new File(path);
		File[] listFiles = dir.listFiles();
		if(listFiles == null) return;
		for (File file : listFiles) {
			if(!file.isDirectory() && (file.getName().contains(".h") || file.getName().contains(".cpp"))) {
				files.add(file.getPath());
				filenames.add(file.getName());
			}			
			else 
				allFiles(file.getPath());
		}
	}
	
	
	static class FilePath {
		public String prefix;
		public String intoDir;
		public String name;
	}
}
