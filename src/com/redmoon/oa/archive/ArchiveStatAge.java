package com.redmoon.oa.archive;

public class ArchiveStatAge {
    private String age;
    private int count;

    public ArchiveStatAge(String age, int count) {
        this.age = age;
        this.count = count;
    }

	public String getAge() {
		return age;
	}

	public void setAge(String age) {
		this.age = age;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

}
