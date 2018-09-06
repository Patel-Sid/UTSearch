package apacheLucene;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.document.Field;

@WebServlet("/search")
public class CC01Query extends HttpServlet {

	   private static StandardAnalyzer analyzer=null;
	   private static Directory index=null;
	   private static final long serialVersionUID = 1L;

	   private static void addDoc(IndexWriter w, String id, String contents) throws IOException {
		   Document doc = new Document();
		   doc.add(new TextField("id", id, Field.Store.YES));
		   doc.add(new TextField("contents", contents, Field.Store.YES));
		   w.addDocument(doc);
		 }
		  
	   
	   public void init(ServletConfig config1) throws ServletException {
		    super.init(config1);
		    System.out.println("Started: init");
		    startIndexing();
		    System.out.println("Finished: init");
		  }


	private void startIndexing() {
		/*
		 * 1. analyzer is used for parsing the data out. 
		 * the standardAnalyzer will remove the stop words. 
		 * 
		 * 2. RamDirectory is the indexer that will be maintained in 
		 * the RAM
		 */
		// Having CharArraySet as empty will make sure stop words are being searched
		// Might want to remove this later on if we find a better way to query
		analyzer = new StandardAnalyzer(CharArraySet.EMPTY_SET);
		index = new RAMDirectory();

		/*
		 * IndexWriterConfig is the configuration that will be used 
		 * when creating the config. I am choosing the default one. 
		 */
		IndexWriterConfig config = new IndexWriterConfig(analyzer);

		try
		{
			/*
			 * IndexWriter is used to write into the index. 
			 */
		IndexWriter w = new IndexWriter(index, config);
		addDoc(w, "My book", "hello my name is sid");
		addDoc(w, "My cool book", "very cool book to read");
		addDoc(w, "Lucene for Dummies", "sid is my name");
		addDoc(w, "Lucene is cool", "the kid");
		addDoc(w, "Managing Gigabytes", "I like to go on long drives");
		addDoc(w, "The Art of Computer Science", "I don't understand code");
		addDoc(w, "The Art of Physical Science", "I don't understand physics");
		w.close();
		}
		catch (IOException e)
		{
				System.err.println(e);
		}
	}
	   @Override
	   protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
	         throws ServletException, IOException {
	   	resp.setContentType("text/plain");
			StringBuilder my_string = new StringBuilder();
		  String querystr = req.getParameter("query");
		  my_string.append("This is my query: " + querystr + "\n");
		  resp.getWriter().write(my_string.toString());
	      try {
	    	  /*
	    	   * 1. Query object, created that encapusulates the user query
	    	   * 
	    	   * 2. IndexReader that allows you to read the index. 
	    	   * 
	    	   * 3. IndexSearcher that allows you to take the query and search the
	    	   *    index. 
	    	   */
	    	  // Store the fields in an array and pass that as the first param, second is the analyzer
			MultiFieldQueryParser q = new MultiFieldQueryParser(new String[]{"id","contents"}, analyzer);
			// By default MultiFieldQueryParser uses OR operator
					// Toggle the below line to switch it to AND operator so both words must exist
			q.setDefaultOperator(MultiFieldQueryParser.Operator.AND);
				long startTime = System.currentTimeMillis();
			Query final_q = q.parse(querystr);
			// Querying with more than one word
			//BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
			//PhraseQuery.Builder my_query = new PhraseQuery.Builder();
			//String[] words = querystr.split(" ");
			//for (String word : words) {
				// This will search the query in id but can be changed to search for contents
				//my_query.add(new Term("id", word));
			//}
			// Distance between the words is set by slop, the lower the number the closer the words
			//my_query.setSlop(2);
			//booleanQuery.add(my_query.build(), Occur.MUST);
			//PhraseQuery final_query = my_query.build();
			//BooleanQuery final_query = booleanQuery.build();
			int hitsPerPage = 100;
			IndexReader reader = DirectoryReader.open(index);
			IndexSearcher searcher = new IndexSearcher(reader);
			TopDocs docs = searcher.search(final_q, hitsPerPage);
			long endTime = System.currentTimeMillis();
			ScoreDoc[] hits = docs.scoreDocs;
			StringBuilder responseBackToUser=new StringBuilder();
			long timeTaken = endTime - startTime;
			responseBackToUser.append("Found " + hits.length + " hits. Time " + timeTaken + "ms \n");
			for(int i=0;i<hits.length;++i) {
			    int docId = hits[i].doc;
			    Document d = searcher.doc(docId);
			    responseBackToUser.append((i + 1) + ". " + d.get("id") + "\t" + d.get("contents") + "\n");
			}
			resp.getWriter().write(responseBackToUser.toString());


			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	      
	      
	   }
	}