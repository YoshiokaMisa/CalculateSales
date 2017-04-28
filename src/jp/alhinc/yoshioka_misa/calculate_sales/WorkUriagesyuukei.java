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

		try{
			File file = new File (args[0], "branch.lst");
			if (!file.exists()) {
	            System.out.println("支店定義ファイルが存在しません");
	            return;
	        }
			FileReader fr = new FileReader (file);
			br = new BufferedReader(fr);

			String s;
			while((s= br.readLine())!= null){
				String[] array= s.split(",");
				if(!array[0].matches("[0-9]{3}$") || array.length != 2){
					System.out.println("支店定義ファイルのフォーマットが不正です");
					return;
				}
				branchnamemap.put(array[0],array[1]);
				branchsalemap.put(array[0],0L);
			}
		}catch (IOException e) {
			System.out.println("予期せぬエラーが発生しました");
			return;
		}finally{
			try{
				if(br !=null){
					br.close();
				}
			}
			catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました");
				return;
			}
		}


		HashMap<String, String>  commoditynamemap = new HashMap< String, String>();
		HashMap<String, Long>  commoditysalemap = new HashMap< String, Long>();
		try{
			File file = new File (args[0],"commodity.lst");
			if (!file.exists()) {
	            System.out.println("商品定義ファイルが存在しません");
	            return;
	        }
			FileReader fr = new FileReader (file);
			br =new BufferedReader(fr);

			String s;
			while((s= br.readLine())!= null){
				String[]array = s.split(",");
				String i = array[0];
				if(!i.matches("[A-Z0-9]{8}") || (array.length != 2 )){
					System.out.println("商品定義ファイルのフォーマットが不正です");
					return;
				}
				commoditynamemap.put(array[0],array[1]);
				commoditysalemap.put(array[0], 0L);
			}
		}catch (IOException e) {
			System.out.println("予期せぬエラーが発生しました");
			return;
		}finally{
			try{
				if(br !=null){
					br.close();
				}
			}
			catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました");
				return;
			}
		}

		File dir = new File(args[0]);
		ArrayList<File> rcdFiles =new ArrayList<File>();
		File[] files = dir.listFiles();

		for (int i = 0; i <files.length; i ++){
			if (files[i].isFile() &&
				(files[i].getName().matches("[0-9]{8}.rcd"))){
				rcdFiles.add(files[i]);
			}
		}

		for(int i =0 ;i<rcdFiles.size()-1; i++){
			String r= rcdFiles.get(i).getName();
			String ren= r.substring(0, 8);
			int Code= Integer.parseInt(ren);

			String rr=rcdFiles.get(i+1).getName();
			String rren= rr.substring(0, 8);
			int Code2= Integer.parseInt(rren);

			if( Code2- Code != 1){
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

				while((s= br.readLine())!= null){
					rcdRead.add(s);
				}
				String branchCode = rcdRead.get(0);
				branchsalemap.get(branchCode);
				String commodityCode =rcdRead.get(1);
				commoditysalemap.get(commodityCode);


				if(!branchsalemap.containsKey(branchCode)){
					System.out.println(rcdFiles.get(i).getName()+"の支店コードが不正です");
					return;
				}
				if(!commoditysalemap.containsKey(commodityCode)){
					System.out.println(rcdFiles.get(i).getName()+"の商品コードが不正です");
					return;
				}
				if(rcdRead.size() !=3){
					System.out.println(rcdFiles.get(i).getName()+"のフォーマットが不正です");
					return;
				}



				long siten = Long.parseLong(rcdRead.get(2));
				long Btotal =siten + branchsalemap.get(branchCode);
				branchsalemap.put(branchCode,Btotal);



				long syouhin = Long.parseLong(rcdRead.get(2));
				long Ctotal = syouhin +  commoditysalemap.get(commodityCode);
				commoditysalemap.put(commodityCode,Ctotal);

				if(Btotal >10000000000L){
					System.out.println("合計金額が10桁を超えました");
					return;
				}

			}
			catch (FileNotFoundException a) {
				System.out.println("予期せぬエラーが発生しました");
				return;
			}catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました");
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

		List<Entry<String,Long>> branchtotal =
			new ArrayList<Entry<String,Long>>(branchsalemap.entrySet());
			Collections.sort(branchtotal, new Comparator<Entry<String,Long>>() {
				public int compare(
				Entry<String,Long> entry1, Entry<String,Long> entry2) {
				return ((Long)entry2.getValue()).compareTo((Long)entry1.getValue());
				}
			});

		File file = new File(args[0],"branch.out");

		BufferedWriter bw = null;

		try{
			FileWriter fw;
			fw = new FileWriter(file);
			bw =new BufferedWriter(fw);
			for(Entry<String, Long> entry : branchtotal){
				bw.write(entry.getKey()+","+branchnamemap.get(entry.getKey())+","+entry.getValue());
				bw.newLine();
			}
		}catch (IOException e) {
			System.out.println("予期せぬエラーが発生しました");
			return;
		}finally{
			try{
				if(bw !=null){
					bw.close();
				}
			}catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました");
				return;
			}
		}

		List<Entry<String,Long>> commoditytotal =
			new ArrayList<Entry<String,Long>>(commoditysalemap.entrySet());
		Collections.sort(commoditytotal, new Comparator<Entry<String,Long>>() {
			public int compare(
				Entry<String,Long> entry1, Entry<String,Long> entry2) {
				return ((Long)entry2.getValue()).compareTo((Long)entry1.getValue());
			}
		});


		try{
			File File = new File(args[0],"commodity.out");

			FileWriter fw = new FileWriter(File);
			bw = new BufferedWriter(fw);

			for(Entry<String, Long> entry : branchtotal){
				bw.write(entry.getKey()+","+branchnamemap.get(entry.getKey())+","+entry.getValue());
				bw.newLine();
			}
		}catch (IOException e) {
			System.out.println("予期せぬエラーが発生しました");
			return;
		}finally{
			try{
				if(bw !=null){
					bw.close();
				}
			}catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました");
				return;
			}
		}
	}
}






