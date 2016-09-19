package com.sameh.goeuro.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CSVWriter {
	
	private List<CSVRow> rows;
	int currentRowIndex;
	private CSVRow currentRow;
	
	String filePath;
	
	public CSVWriter(String filePath) {
		rows = new ArrayList<>();
		rows.add(new CSVRow());
		currentRowIndex = 0;
		currentRow = rows.get(currentRowIndex);
		this.filePath = filePath;
	}
	
	public void addValue(Object value) {
		currentRow.addValue(value);
	}
	
	public void nextRow() {
		currentRowIndex++;
		if (currentRowIndex >= rows.size()) {
			rows.add(new CSVRow());
		}
		currentRow = rows.get(currentRowIndex);
	}
	
	public void previousRow() {
		if (currentRowIndex == 0) {
			return;
		}
		currentRowIndex--;
		currentRow = rows.get(currentRowIndex);
	}
	
	public void setRowTo(int index) {
		if (index >= rows.size()) {
			for (int i = rows.size(); i <= index; i++) {
				rows.add(new CSVRow());
			}
		}
		currentRowIndex = index;
		currentRow = rows.get(currentRowIndex);
	}
	
	public void write() throws IOException {
		write(filePath);
	}
	
	public void write(String filePath) throws IOException {
		System.out.println("Saving to '" + filePath.replace("'", "\\'") + "'...");
		File file = new File(filePath);
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		
		for (CSVRow row : rows) {
			boolean first = true;
			for (String value : row) {
				if (!first) {
					bw.write(",");
				} else {
					first = false;
				}
				if (row.isInQuotes()) {
					bw.write("\"");
					bw.write(value.replace("\"", "\"\""));
					bw.write("\"");
				} else {
					bw.write(value);
				}
			}
			bw.newLine();
		}
		
		bw.close();
		
		System.out.println("Saved.");
	}
	
	private class CSVCell {
		private String value;
		private boolean inQuotes;
		
		public CSVCell(Object value) {
			this.value = value != null ? value.toString() : "";
			inQuotes = this.value.contains(",") || this.value.contains("\"");
		}
		
		public String getValue() {
			return value;
		}
		
		public boolean isInQuotes() {
			return inQuotes;
		}
	}
	
	private class CSVRow implements Iterable<String> {
		private List<CSVCell> cells;
		private boolean inQuotes;
		
		public CSVRow() {
			cells = new ArrayList<>();
			inQuotes = false;
		}
		
		public void addCell(CSVCell cell) {
			cells.add(cell);
			if (cell.isInQuotes()) {
				inQuotes = true;
			}
		}
		
		public void addValue(Object value) {
			CSVCell cell = new CSVCell(value);
			addCell(cell);
		}
		
		public String getValue(int index) {
			return cells.get(index).getValue();
		}
		
		public boolean isInQuotes() {
			return inQuotes;
		}

		@Override
		public Iterator<String> iterator() {
			Iterator<String> it = new Iterator<String>() {
				private Iterator<CSVCell> localIt = cells.iterator();
				
				@Override
				public boolean hasNext() {
					return localIt.hasNext();
				}

				@Override
				public String next() {
					return ((CSVCell)localIt.next()).getValue();
				}
				
			};
			
			return it;
		}
	}
}
