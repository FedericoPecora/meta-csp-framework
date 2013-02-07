/*******************************************************************************
 * Copyright (c) 2010-2013 Federico Pecora <federico.pecora@oru.se>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package symbols;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Vector;

import framework.Domain;

public class SymbolicDomain extends Domain implements Serializable{
	private String[] symbols = null;
	private boolean[] masked = null;
	
	private static final long serialVersionUID = 7526472295622776129L;
		
	public SymbolicDomain(SymbolicVariable v, String ...symbols) {
		super(v);
		this.symbols = symbols;
		masked = new boolean[symbols.length];
		for (int i = 0; i < masked.length; i++) masked[i] = false;
	}

	public void setMask(String symbol, boolean value) {
		int index = -1;
		for (int i = 0; i < symbols.length; i++)
			if (symbols[i].equals(symbol)) {
				index = i;
				break;
			}
		if (index != -1) this.masked[index] = value;
	}
	
	public boolean isMasked(String symbol) {
		int index = -1;
		for (int i = 0; i < symbols.length; i++)
			if (symbols[i].equals(symbol)) {
				index = i;
				break;
			}
		if (index != -1) return this.masked[index];
		return false;
	}
	
	public void resetMasks() {
		for (int i = 0; i < masked.length; i++) masked[i] = false;
	}
	
	public String[] getSymbols() {
		Vector<String> ret = new Vector<String>();
		for (int i = 0; i < symbols.length; i++)
			if (!masked[i]) ret.add(symbols[i]);
		return ret.toArray(new String[ret.size()]);
	}
	
	public String toString() {
		Vector<String> ret = new Vector<String>();
		for (int i = 0; i < symbols.length; i++)
			if (!masked[i]) ret.add(symbols[i]);
		return Arrays.toString(ret.toArray(new String[ret.size()]));
	}

	@Override
	protected void registerValueChoiceFunctions() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int compareTo(Object arg0) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public static SymbolicDomain intersection(SymbolicDomain d1, SymbolicDomain d2) {
		Vector<String> dom1 = new Vector<String>();
		for (String s : d1.getSymbols()) dom1.add(s);
		Vector<String> dom2 = new Vector<String>();
		for (String s : d2.getSymbols()) dom2.add(s);
		dom1.retainAll(dom2);
		SymbolicDomain ret = new SymbolicDomain(null, dom1.toArray(new String[dom1.size()]));
		return ret;
	}
	
	public boolean containsSymbol(String value){
		
		if(this.symbols==null){ return false;}
		for(String s: symbols){
			if(s.equals(value)){return true;}
		}
		return true;
	}
	
	public static SymbolicDomain union(SymbolicDomain d1, SymbolicDomain d2) {
		Vector<String> dom1 = new Vector<String>();
		for (String s : d1.getSymbols()) dom1.add(s);
		for (String s : d2.getSymbols()) dom1.add(s);
		SymbolicDomain ret = new SymbolicDomain(null, dom1.toArray(new String[dom1.size()]));
		return ret;
	}
}
