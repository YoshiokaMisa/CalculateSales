package jp.alhinc.yoshioka_misa.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;


public class WorkUriagesyuukei {

	public static void main(String[] args) {

		if(args.length!=1){
			System.out.println("予期せぬエラーが発生しました");
			return;
		}

		HashMap<String, String> branchNameMap = new HashMap<>();
		HashMap<String, Long> branchSaleMap = new HashMap<>();
		BufferedReader br =null;

		HashMap<String, String> commodityNameMap = new HashMap<>();
		HashMap<String, Long> commoditySaleMap = new HashMap<>();


		if(!fileRead(args[0], "branch.lst", "支店", "[0-9]{3}$", branchNameMap, branchSaleMap)){
			return;
		}
		if(!fileRead(args[0], "commodity.lst", "商品", "[A-Za-z0-9]{8}", commodityNameMap, commoditySaleMap)){
			return;
		}

		File dir = new File(args[0]);
		ArrayList<File> rcdFiles = new ArrayList<File>();
		File[] files = dir.listFiles();
		for (int i = 0; i <files.length; i ++){
			if (files[i].isFile() && (files[i].getName().matches("[0-9]{8}.rcd"))){
				rcdFiles.add(files[i]);
			}
		}
		for(int i = 0 ; i < rcdFiles.size()-1; i++){
			int number = Integer.parseInt(rcdFiles.get(i).getName().substring(0, 8));
			int nextNumber = Integer.parseInt(rcdFiles.get(i + 1).getName().substring(0, 8));

			if(nextNumber - number != 1){
				System.out.println("売上ファイル名が連番になっていません");
				return;
			}
		}

		for(int i = 0; i <rcdFiles.size(); i++) {
			ArrayList<String> rcdRead = new ArrayList<String>();

			try{
				String s;
				FileReader fr = new FileReader(rcdFiles.get(i));
				br =new BufferedReader(fr);

				while((s = br.readLine())!= null){
					rcdRead.add(s);
				}
				String branchCode = rcdRead.get(0);
				String commodityCode = rcdRead.get(1);

				if(rcdRead.size() !=3){
					System.out.println(rcdFiles.get(i).getName()+"のフォーマットが不正です");
					return;
				}
				if(!branchSaleMap.containsKey(branchCode)){
					System.out.println(rcdFiles.get(i).getName()+"の支店コードが不正です");
					return;
				}
				if(!commoditySaleMap.containsKey(commodityCode)){
					System.out.println(rcdFiles.get(i).getName()+"の商品コードが不正です");
					return;
				}

				if(!rcdRead.get(2).matches("[0-9]+$")){
					System.out.println("予期せぬエラーが発生しました");
					return;
				}

				long branch = Long.parseLong(rcdRead.get(2));
				Long branchTotal = branch + branchSaleMap.get(branchCode);


				long commodity = Long.parseLong(rcdRead.get(2));
				Long commodityTotal = commodity + commoditySaleMap.get(commodityCode);


				if(branchTotal.toString().length() > 10 || commodityTotal.toString().length() > 10){
					System.out.println("合計金額が10桁を超えました");
					return;
				}
				branchSaleMap.put(branchCode,branchTotal);
				commoditySaleMap.put(commodityCode,commodityTotal);

			}catch (FileNotFoundException a) {
				System.out.println("予期せぬエラーが発生しました");
				return;
			}catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました");
				return;
			}finally{
				try{
					if(br !=null){
						br.close();
					}
				}catch (IOException e) {
					System.out.println("予期せぬエラーが発生しました");
					return;
				}
			}
		}


		if(!fileOut(args[0], "branch.out", branchNameMap, branchSaleMap)){
			return;
		}
		if(!fileOut(args[0], "commodity.out", commodityNameMap, commoditySaleMap)){
			return;
		}
	}

	public static boolean fileRead(String dirPath, String fileName, String fileExist, String code,
			HashMap<String, String> nameMap, HashMap<String, Long> saleMap){
		BufferedReader br = null;
		try{
			File file = new File (dirPath, fileName);
			if (!file.exists()) {
				System.out.println(fileExist + "定義ファイルが存在しません");
	            return false;
	        }
			FileReader fr = new FileReader (file);
			br = new BufferedReader(fr);

			String s;
			while((s = br.readLine()) != null){
				String[] array = s.split(",");
				if(array.length != 2 || !array[0].matches(code)){
					System.out.println(fileExist + "定義ファイルのフォーマットが不正です");
					return false;
				}

				nameMap.put(array[0],array[1]);
				saleMap.put(array[0],0L);
			}
		}catch (IOException e) {
			System.out.println("予期せぬエラーが発生しました");
			return false;
		}finally{
			try{
				if(br != null){
					br.close();
				}
			}
			catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました");
				return false;
			}
		}
		return true;
	}

	public static boolean fileOut(String dirPath, String fileName,
			HashMap<String, String> nameMap, HashMap<String, Long> saleMap){
		List<Entry<String, Long>> total =
				new ArrayList<Entry<String, Long>>(saleMap.entrySet());
		Collections.sort(total, new Comparator<Entry<String,Long>>() {
			public int compare(
			Entry<String, Long> entry1, Entry<String, Long> entry2) {
			return (entry2.getValue()).compareTo(entry1.getValue());
			}
		});

		File file = new File(dirPath,fileName);
		BufferedWriter bw = null;

		try{
			FileWriter fw;
			fw = new FileWriter(file);
			bw = new BufferedWriter(fw);
			for(Entry<String, Long> entry : total){
				bw.write(entry.getKey() + "," + nameMap.get(entry.getKey()) + "," + entry.getValue());
				bw.newLine();
			}
		}catch (IOException e) {
			System.out.println("予期せぬエラーが発生しました");
			return false;
		}finally{
			try{
				if(bw != null){
					bw.close();
				}
			}catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました");
				return false;
			}
		}
		return true;
	}
}
