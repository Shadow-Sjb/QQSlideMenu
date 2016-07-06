package com.sjb.tencent.tencentqq.swipe;

import com.sjb.tencent.tencentqq.swipe.SwipeLayout.Status;

public interface SwipeLayoutInterface {

	Status getCurrentStatus();
	
	void close();
	
	void open();
}
