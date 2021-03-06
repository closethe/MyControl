package com.example.yuansen.baiduimaptest.model;

import android.graphics.Bitmap;

public class MarkObject {

	private Bitmap mBitmap;
	private float mapX;
	private float mapY;
	private float currentX;
	private float currentY;

	private MarkClickListener listener;

	public MarkObject() {

	}

	public MarkObject(Bitmap mBitmap, float mapX, float mapY) {
		super();
		this.mBitmap = mBitmap;
		this.mapX = mapX;
		this.mapY = mapY;
	}

	/**
	 * @return the mBitmap
	 */
	public Bitmap getmBitmap() {
		return mBitmap;
	}

	/**
	 * @param mBitmap
	 *            the mBitmap to set
	 */
	public void setmBitmap(Bitmap mBitmap) {
		this.mBitmap = mBitmap;
	}

	/**
	 * @return the mapX
	 */
	public float getMapX() {
		return mapX;
	}

	/**
	 * @param mapX
	 *            the mapX to set
	 */
	public void setMapX(float mapX) {
		this.mapX = mapX;
	}

	/**
	 * @return the mapY
	 */
	public float getMapY() {
		return mapY;
	}

	/**
	 * @param mapY
	 *            the mapY to set
	 */
	public void setMapY(float mapY) {
		this.mapY = mapY;
	}
	
	public MarkClickListener getMarkListener() {
		return listener;
	}

	public void setMarkListener(MarkClickListener listener) {
		this.listener = listener;
	}

	public float getCurrentX() {
		return currentX;
	}

	public void setCurrentX(float currentX) {
		this.currentX = currentX;
	}

	public float getCurrentY() {
		return currentY;
	}

	public void setCurrentY(float currentY) {
		this.currentY = currentY;
	}

	public void setCurrent(float currentX,float currentY){
		this.currentX = currentX;
		this.currentY = currentY;
	}
	public interface MarkClickListener {
		public void onMarkClick(int x, int y);
	}

}
