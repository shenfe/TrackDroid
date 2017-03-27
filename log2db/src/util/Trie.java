package util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class Trie {

    private static final TrieNode[] EMPTYNODES = new TrieNode[0];

    private static final class TrieNode implements Comparable<TrieNode> {

        private final String word;
        private int count;
        private TrieNode parent = null;
        private Map<String, TrieNode> children = null;
        public static boolean ifSort = false;

        public TrieNode(String s) {
            word = s;
            count = 1;
        }
        
        public void union(TrieNode node) {
        	if(node == null) return;
        	count++;
        	if(node.children == null) {
            	return;
            }
		    Iterator iter = node.children.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				Object key = entry.getKey();
				Object val = entry.getValue();
				if(children != null && children.containsKey(key)) {
					children.get(key).union((TrieNode) val);
				} else {
					if(children == null) children = new HashMap<String, TrieNode>();
					children.put((String) key, (TrieNode) val);
				}
			}
        }
        
        public TrieNode getParent() {
        	return parent;
        }

        public TrieNode getOrCreateChild(String s, boolean countInc) {
            if (children == null) {
                children = new HashMap<String, TrieNode>();
            }
            TrieNode kid = children.get(s);
            if (kid == null) {
                kid = new TrieNode(s);
                children.put(s, kid);
                kid.parent = this;
            }
            if(countInc) count++;
            return kid;
        }

        public TrieNode get(String s) {
            return children != null ? children.get(s) : null;
        }

        public String getWord() {
            return word;
        }
        
        public List<String> getWords() {
            List<String> result = new ArrayList<String>();
            if(children == null) {
            	result.add(word);
            	return result;
            }
		    Iterator iter = children.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				Object key = entry.getKey();
				Object val = entry.getValue();
				List<String> subresult = ((TrieNode)val).getWords();
				for(String wd : subresult) {
					if(wd.equals("")) continue;
					result.add(word + (word.equals("") ? "" : ".") + wd);
				}
			}
			return result;
        }
        
        public List<String> getWords(int d) {
            List<String> result = new ArrayList<String>();
            if(d <= 0) return result;
            if(d == 1) {
        		result.add(word);
        		return result;
        	}
            if(children == null || children.size() == 0) {
            	return null;
            }
		    Iterator iter = children.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				Object key = entry.getKey();
				Object val = entry.getValue();
				List<String> subresult = ((TrieNode)val).getWords(d - 1);
				if(subresult == null) continue;
				for(String wd : subresult) {
					if(wd.equals("")) continue;
					result.add(word + (word.equals("") ? "" : ".") + wd);
				}
			}
			return result;
        }

        public TrieNode[] getChildNodes(int countLimit) {
            if (children == null) {
                return EMPTYNODES;
            }
            ArrayList<TrieNode> arr = new ArrayList<TrieNode>();
		    Iterator iter = children.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				Object key = entry.getKey();
				Object val = entry.getValue();
				if(((TrieNode)val).count < countLimit) continue;
				arr.add((TrieNode)val);
			}
			TrieNode[] result = arr.toArray(new TrieNode[children.size()]);
            if(ifSort) Arrays.sort(result);
            return result;
        }
        
        public TrieNode[] getChildNodes(boolean allDeeper, int countLimit) {
        	if(!allDeeper) return getChildNodes(countLimit);
            if (children == null) {
                return EMPTYNODES;
            }
            Collection<TrieNode> arr = getAllChildNodes(countLimit);
            TrieNode[] result = arr.toArray(new TrieNode[arr.size()]);
            if(ifSort) Arrays.sort(result);
            return result;
        }
        public Collection<TrieNode> getAllChildNodes(int countLimit) {
            if (children == null) {
                return null;
            }
            ArrayList<TrieNode> result = new ArrayList<TrieNode>();
		    Iterator iter = children.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				Object key = entry.getKey();
				Object val = entry.getValue();
				if(((TrieNode)val).count < countLimit) continue;
				result.add((TrieNode)val);
				Collection<TrieNode> subresult = ((TrieNode)val).getAllChildNodes(countLimit);
				if(subresult == null) continue;
				for(TrieNode nd : subresult) {
					if(nd.count < countLimit) continue;
					result.add(nd);
				}
			}
			return result;
        }
        
        public TrieNode[] getChildNodes(int d, boolean allDeeper, int countLimit) {
            if (children == null) {
                return EMPTYNODES;
            }
            if(d == 1) return getChildNodes(allDeeper, countLimit);
            List<TrieNode> nodes = new ArrayList<TrieNode>();
            
            Iterator iter = children.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				Object key = entry.getKey();
				Object val = entry.getValue();
				if(((TrieNode)val).count < countLimit) continue;
				TrieNode[] subnodes = ((TrieNode)val).getChildNodes(d - 1, allDeeper, countLimit);
				for(TrieNode nd : subnodes) {
					if(nd == null) continue;
					nodes.add(nd);
				}
			}
			
            TrieNode[] result = nodes.toArray(new TrieNode[nodes.size()]);
            if(ifSort) Arrays.sort(result);
            return result;
        }
        
        public String getPackageString() {
        	TrieNode nd = this;
        	List<String> words = new ArrayList<String>();
        	words.add(nd.word);
        	while(nd.parent != null && !nd.parent.word.equals("")) {
        		nd = nd.parent;
        		words.add(nd.word);
        	}
        	Collections.reverse(words);
        	return String.join(".", words);
        }

        @Override
        public int compareTo(TrieNode o) {
            return o.count - count;
        }

    }

    private final TrieNode root;  // fix - make root final
    private int depth = 0; // longest package

    public Trie(){
        root = new TrieNode("");
    }
    
    public void union(Trie tree) {
    	root.union(tree.root);
    }

    public void addPackage(String pkg){
    	if(pkg == null || pkg.equals("")) return;
    	String[] words = pkg.replace('/', '.').split("\\.");
        TrieNode node = root;
        int wdepth = 0;
        for (String wd : words) {
        	if(wd.equals("")) continue;
            node = node.getOrCreateChild(wd, false);
            wdepth++;
        }
        if (wdepth > depth) {
            depth = wdepth;
        }
    }

    public int containPackage(String pkg){
    	if(pkg == null || pkg.equals("")) return 0;
    	String[] words = pkg.replace('/', '.').split("\\.");
        TrieNode node = root;
        for (String wd : words) {
            node = node.get(wd);
            if (node == null) {
                break;
            }
        }
        return node != null ? node.count : 0;
    }

    public List<String> getPackages() {
        return root.getWords();
    }
    
    public List<String> getPackages(int d) {
        return root.getWords(d + 1);
    }
    
    public TrieNode[] getPackageNodes(int d, boolean allDeeper, int countLimit) {
    	if(d <= 0) return EMPTYNODES;
        return root.getChildNodes(d, allDeeper, countLimit);
    }
    
    public void outputPackages(int d, boolean allDeeper, int countLimit) {
    	TrieNode[] nodes = this.getPackageNodes(d, allDeeper, countLimit);
        for(TrieNode nd : nodes){
            System.out.println(Integer.toString(nd.count) + ": " + nd.getPackageString());
        }
    }

    /*
    public static void main(String[] args) {
        Trie t = new Trie();
        t.addPackage("1.2.3.4.5.6.7.");
        t.addPackage("h.ha");
        t.addPackage("h.ha.ggg");
        t.addPackage("h.a.ggg");
        t.addPackage("sam.sung.mobile.phone");
        t.addPackage("sampson");
        t.addPackage("Double.Vision");

        List<String> words = t.getPackages(4);
        for(String s : words){
            System.out.println(s);
        }
        
        TrieNode[] nodes = t.getPackageNodes(3);
        for(TrieNode nd : nodes){
            System.out.println(nd.getPackageString());
        }
        
        System.out.println();
        System.out.println("h:" + t.containPackage("h"));
        System.out.println("h.ha:" + t.containPackage("h.ha"));
        System.out.println("ha:" + t.containPackage("ha"));
        System.out.println();
        System.out.println(t.containPackage("samsun"));
    }
    */

}