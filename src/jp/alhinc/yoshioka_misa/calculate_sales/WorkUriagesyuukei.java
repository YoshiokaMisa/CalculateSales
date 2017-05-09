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

		HashMap<String, String>  branchnamemap = new HashMap< String, String>();
		HashMap<String, Long>  branchsalemap = new HashMap< String, Long>();
		BufferedReader br =null;

		HashMap<String, String>  commoditynamemap = new HashMap< String, String>();
		HashMap<String, Long>  commoditysalemap = new HashMap< String, Long>();


		if(!fileRead(args[0], "branch.lst", "支店", "[0-9]{3}$", branchnamemap, branchsalemap)){
			return;
		}
		if(!fileRead(args[0], "commodity.lst", "商品", "[A-Za-z0-9]{8}", commoditynamemap, commoditysalemap)){
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
		for(int i =0 ;i<rcdFiles.size()-1; i++){
			int Number = Integer.parseInt(rcdFiles.get(i).getName().substring(0, 8));
			int NextNumber = Integer.parseInt(rcdFiles.get(i+1).getName().substring(0, 8));

			if( NextNumber- Number != 1){
				System.out.println("売上ファイル名が連番になっていません");
				return;
			}
		}

		for(int i = 0; i <rcdFiles.size(); i++) {
			ArrayList<String> rcdRead = new ArrayList<String>();

			try{
				String s;
				 FileReader fr = new FileReader (rcdFiles.get(i));
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
				if(!branchsalemap.containsKey(branchCode)){
					System.out.println(rcdFiles.get(i).getName()+"の支店コードが不正です");
					return;
				}
				if(!commoditysalemap.containsKey(commodityCode)){
					System.out.println(rcdFiles.get(i).getName()+"の商品コードが不正です");
					return;
				}

				if(!rcdRead.get(2).matches("[0-9]+$")){
					System.out.println("予期せぬエラーが発生しました");
					return;
				}

				long siten = Long.parseLong(rcdRead.get(2));
				long Btotal = siten + branchsalemap.get(branchCode);


				long syouhin = Long.parseLong(rcdRead.get(2));
				long Ctotal = syouhin +  commoditysalemap.get(commodityCode);


				if(Btotal >= 10000000000L || Ctotal >= 10000000000L){
					System.out.println("合計金額が10桁を超えました");
					return;
				}
				branchsalemap.put(branchCode,Btotal);
				commoditysalemap.put(commodityCode,Ctotal);

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


		if(!fileOut(args[0], "branch.out", branchnamemap, branchsalemap)){
			return;
		}
		if(!fileOut(args[0], "commodity.out", commoditynamemap, commoditysalemap)){
			return;
		}
	}

	public static boolean fileRead (String dirpath, String fileName, String fileExist, String code,
			HashMap<String, String>  namemap, HashMap<String, Long>  salemap){
		BufferedReader br = null;
		try{
			File file = new File (dirpath, fileName);
			if (!file.exists()) {
				System.out.println(fileExist + "定義ファイルが存在しません");
	            return false;
	        }
			FileReader fr = new FileReader (file);
			br = new BufferedReader(fr);

			String s;
			while((s = br.readLine()) != null){
				String[] array = s.split(",");
				if( array.length != 2 || !array[0].matches(code)){
					System.out.println(fileExist + "定義ファイルのフォーマットが不正です");
					return false;
				}

				namemap.put(array[0],array[1]);
				salemap.put(array[0],0L);
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

	public static boolean fileOut (String dirpath, String fileName,
			HashMap<String, String>  namemap, HashMap<String, Long>  salemap){
		List<Entry<String, Long>> total =
				new ArrayList<Entry<String, Long>>(salemap.entrySet());
				Collections.sort(total, new Comparator<Entry<String,Long>>() {
					public int compare(
					Entry<String, Long> entry1, Entry<String, Long> entry2) {
					return ((Long)entry2.getValue()).compareTo((Long)entry1.getValue());
					}
				});

		File file = new File(dirpath,fileName);
		BufferedWriter bw = null;

		try{
			FileWriter fw;
			fw = new FileWriter(file);
			bw = new BufferedWriter(fw);
			for(Entry<String, Long> entry : total){
				bw.write(entry.getKey() + "," + namemap.get(entry.getKey()) + "," + entry.getValue());
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






