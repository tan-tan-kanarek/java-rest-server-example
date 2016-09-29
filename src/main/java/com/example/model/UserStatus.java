package com.example.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

@XmlEnum(Integer.class)
public enum UserStatus {
	@XmlEnumValue("0")
	Active,
	@XmlEnumValue("1")
	Disabled;
	
	public static UserStatus fromValue(int value)
	{
		switch(value)
		{
		case 0:
			return Active;
			
		case 1:
			return Disabled;
		}
		
		return Active;
	}
}
