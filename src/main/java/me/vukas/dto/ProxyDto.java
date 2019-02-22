package me.vukas.dto;

import lombok.Data;

@Data
public class ProxyDto {
	private String name;
	private String listen;
	private String upstream;
}
