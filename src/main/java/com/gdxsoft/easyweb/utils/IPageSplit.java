package com.gdxsoft.easyweb.utils;

/**
 * 分页对象
 * 
 * @author admin
 *
 */
public interface IPageSplit {

	/**
	 * 当前页编号
	 * 
	 * @return the _PageCurrent
	 */
	public int getPageCurrent();

	/**
	 * 总页数
	 * 
	 * @return the _PageCount
	 */
	public int getPageCount();

	/**
	 * 记录数
	 * 
	 * @return the _RecordCount
	 */
	public int getRecordCount();

	/**
	 * 每页的记录数
	 * 
	 * @return the _PageSize
	 */
	public int getPageSize();

}
