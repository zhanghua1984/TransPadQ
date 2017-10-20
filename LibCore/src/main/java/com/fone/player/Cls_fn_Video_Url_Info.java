package com.fone.player;

public class Cls_fn_Video_Url_Info {
	public String out_cid;
	public String out_ccid;
	public String out_xyzplayer;
	public String out_thumb;
	public String out_name;
	public String out_source_url;
	public String out_url_md5;
	public String out_storepath;
	
	public int out_dfnt;
	public int out_is_hd;
	public int out_type;
	public int out_frac_cnt;
	public int out_state;
	public int out_index;
	public int out_reconn_cnt;

	public long out_download_pos;
	public long out_file_size;
	public long out_file_durat;
	public long out_duration;
	public int out_error_code;
	public int out_contentType;
	public Cls_fn_Video_Url_Info(String cid,String ccid,String dfnt,String xyzplayer,String thumb,String name,
			String is_hd,String type,String frac_cnt,String state,String download_pos,String file_size,
			String file_durat,String index,String source_url,String url_md5,String reconn_cnt,String duraton, 
			String storepath,int error_code,int contentType)
	{
		
			out_cid = cid;
			out_ccid = ccid;
			out_contentType=contentType;
			if(dfnt != null){
				out_dfnt=Integer.parseInt(dfnt);
			}else{
				System.out.println("There is no dfnt!");
			}
			out_xyzplayer=xyzplayer;
			out_thumb=thumb;
			out_source_url=source_url;
			out_name=name;
			out_url_md5=url_md5;
			out_storepath = storepath;
			out_error_code=error_code;
			if(is_hd != null){
				out_is_hd = Integer.parseInt(is_hd);
			}else{
				System.out.println("There is no is_hd!");
			}
			if(type != null){
				out_type=Integer.parseInt(type);
			}else{
				System.out.println("There is no type!");
			}
			if(frac_cnt != null){
				out_frac_cnt=Integer.parseInt(frac_cnt);
			}else{
				System.out.println("There is no frac_cnt!");
			}
			if(state != null){
				out_state=Integer.parseInt(state);
			}else{
				System.out.println("There is no state!");
			}
			if(reconn_cnt != null){
				out_reconn_cnt=Integer.parseInt(reconn_cnt);
			}else{
				System.out.println("There is no reconn_cnt!"); 				
			}
			if(index != null){
				out_index=Integer.parseInt(index);
			}else{
				System.out.println("There is no index!");
			}
			if(file_durat != null){
				out_file_durat=Long.parseLong(file_durat);
			}else{
				System.out.println("There is no file_durat!");
			}
			if(duraton != null){
				out_duration=Long.parseLong(duraton);
			}else{
				System.out.println("There is no duration!");
			}
			if(download_pos != null){
				out_download_pos=Long.parseLong(download_pos);
			}else{
				System.out.println("There is no download_pos!");
			}
			if(file_size != null){
				out_file_size=Long.parseLong(file_size);
			}else{
				System.out.println("There is no file_size!");
			}
	}
}
