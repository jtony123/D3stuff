package controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;

import com.avaje.ebean.enhance.agent.SysoutMessageOutput;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import models.Category;
import models.Player;
import models.User;
import play.Configuration;
import play.data.DynamicForm;
import play.data.Form;
import play.data.FormFactory;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import utilities.AcuteChronicUpdater;
import utilities.CSVAppender;
import utilities.CSVLoader3;
import utilities.CSVOutput;
import utilities.CSVRedoxGenerator;
import utilities.CSVSortByTime;
import utilities.CSVTemplateGenerator;
import utilities.GameDataLoader;
import utilities.RedoxCSVLoader;
import utilities.RedoxCSVUpdater;
//import play.db.jpa.*;
import views.html.dashboard;
import views.html.index;
import views.html.Admin.users;
import views.html.redox;

public class Application extends Controller {
	

	
	@Inject 
	private  Configuration configuration;
	String filepath;
	
	

    @Inject
    FormFactory formFactory;
    
    private final HttpExecutionContext ec;

    @Inject
    public Application(final HttpExecutionContext ec, Configuration configuration)
    {
        this.ec = ec;
        this.configuration = configuration;
        filepath = configuration.getString("filepath");
    }
    
 
    
    public CompletionStage<Result> index() {
    	System.out.println("index called");
    	System.out.println("testing reload");
    	User user = User.findByEmail(session().get("connected"));
    	if(user != null) {
    		System.out.println("user found = "+user.email);
    		return CompletableFuture.completedFuture(redirect(routes.Application.dashboard(4, "All")));
    	} 
    	return CompletableFuture.completedFuture(ok(index.render()));
    }
    
    @Restrict({@Group({"admin"})})
    public CompletionStage<Result> admin() {
    	System.out.println("get admin called");
    	User user = User.findByEmail(session().get("connected"));
    	List<User> allusers = User.find.all();
    	return CompletableFuture.completedFuture(ok(users.render(user, "users", allusers)));
    }
    
    

    @Restrict({@Group({"coach"})})
    public CompletionStage<Result> dashboard(int playernumber, String category) {
    	
    	
    	System.out.println("dashboard called");
    	
    	//Player test = Player.findByNameOrAlias("quincyacy");
    	
    	//System.out.println("player alias found is " + test.playername);
    	
    	User user = User.findByEmail(session().get("connected"));
    	Category categoryFound = Category.findByName(category);
    	Player player;
    	List<Player> players;
    	
    	if(categoryFound != null){
    		//System.out.println("cat not null");
    		players = user.getPlayersCategorisedWith(categoryFound);
    		
    		// check for empty category for this user
    		if(players == null || players.isEmpty()){
    		//empty category, return 'no players'
    			//System.out.println("players is null, sending no players");
    			player = Player.findByPlayername("no players");
    			players.add(player);
    		} else {
    		// the category is not empty
    			//System.out.println("players not null");
    	    	if (playernumber == 0) {
    	    		// to handle initial call
    	    		//System.out.println("handle initial call, sending player 0");
    	    		player = players.get(0);
    	    	} else {
    	    		player = Player.findByNumber(playernumber);
    	    		//System.out.println("finding player by number");
    	    		
    	    		// check first if the player is null(out of range)
    	    		if(player == null) {
    	    			player = players.get(0);
    	    		} else {
    	    			// check if the player that the user had highlighted is in this category
        	    		if (!player.categories.contains(categoryFound)){
        	    			//System.out.println("player not in this category, sending player 0");
        	    			player = players.get(0);
        	    		}
    	    		}
    	    		
    	    		 
    	    	}
    		}
    		
    	} else {
    		//System.out.println("cat is null");
    		players = user.getPlayersCategorisedWith(Category.findByName("All"));
    		player = players.get(0);
    		category = "All";
    	}
    	
    	List<Category> categories = Category.find.all();
    	//System.out.println("player count = " + players.size());
    	int playerIndex = players.indexOf(player);
    	//System.out.println("......");
    			
    	return CompletableFuture.completedFuture(ok(dashboard.render(user, "dashboard", player, playerIndex, players, category, categories)));
    	
    }
    

    
    @Restrict({@Group({"coach"})})
    public CompletionStage<Result> redox() {
    	System.out.println("redox called");
    	User user = User.findByEmail(session().get("connected"));
    	
    	int playernumber = 4;
    	String category = "All";
    	Category categoryFound = Category.findByName(category);
    	Player player;
    	List<Player> players = Player.find.all();
    	
    	
    	
    	
    	return CompletableFuture.completedFuture(ok(redox.render(user, "redox", players)));
    }
    
  public Result getFullRedoxCSV(){
    	
    	System.out.println("getFullRedoxCSV called");
   	
   	 return ok(new java.io.File(filepath +"redox.csv"));
    }
    
    
    
    @Restrict({@Group({"coach"})})
    public CompletionStage<Result> record(int playernumber, String category) {
    	System.out.println("record called");
    	User user = User.findByEmail(session().get("connected"));
    	Category categoryFound = Category.findByName(category);
    	List<Player> players = user.getPlayersCategorisedWith(categoryFound);
    	
    	Player player = Player.findByNumber(playernumber);
    	if(player == null) {
    		player = Player.find.byId((long) 1);
    	}
    	
    	List<Category> categories = Category.find.all();
    	//System.out.println("player count = " + players.size());
    	int playerIndex = players.indexOf(player);
    	
    	return CompletableFuture.completedFuture(ok(dashboard.render(user, "dashboard", player, playerIndex, players, category, categories)));
    	
    }
    
    // TODO: work out whether this needs to be restricted, as its only called directly by the page, not the user
    //@Restrict({@Group({"admin", "coach"})})
    public CompletionStage<Result> playerPhoto(Long playerId) {
    	//System.out.println("playerPhoto called");
		final Player player = Player.find.byId(playerId);
    	return CompletableFuture.completedFuture(ok(player.playerPhoto).as("playerPhoto"));
    }
    
    
   public Result getCalendarCSV(){
    	
	    //System.out.println("getCalendarCSV called");
	   	return ok(new java.io.File(filepath + "Schedule1.csv"));
    }
    
    

	// CSV file.
	public CompletionStage<Result> uploadCalender() {

		//System.out.println("uploadCalender called");

		User user = User.findByEmail(session().get("connected"));
		List<User> allusers = User.find.all();

		MultipartFormData<File> body = request().body().asMultipartFormData();
		FilePart<File> filename = body.getFile("filename");
		
		File file = new File(filepath + "Schedule1.csv");
		
		// create the new file
		if ( !file.exists() ){
			try {
				//System.out.println("creating new file");
				file.createNewFile();
			} catch (IOException e1) {
				// TODO return an error message to the user
				//System.out.println("throwing file exist exception");
				e1.printStackTrace();
			}
	      }	
		
		BufferedReader fileReader = null;
		PrintWriter out = null;
		
		if (filename != null) {
			
            File newfile = filename.getFile();
			
			String line ="";
			
			try {
				//System.out.println("filereader here");
				fileReader = new BufferedReader(new FileReader(newfile.getAbsolutePath()));
				//System.out.println("out here");
				out = new PrintWriter(file, "UTF-8");
				//System.out.println("while loop here");
				
				while ((line = fileReader.readLine()) != null) {
					out.println(line);
				}
				
			} catch (Exception e) {
				// TODO return an error message to the user
				e.printStackTrace();
			}	finally {
				out.flush();
				out.close();
			}

			flash("success", "Calendar file uploaded successfully");
			return CompletableFuture.completedFuture(ok(users.render(user, "users", allusers)));
		} else {
			flash("error", "Missing file");
			return CompletableFuture.completedFuture(ok(users.render(user, "users", allusers)));
		}

	}
    
    
   

    public Result getCSV(Integer playernumber){
    	
    	System.out.println("getCSV called");
   	 Player player = Player.findByNumber(playernumber);
   	
   	 return ok(new java.io.File(filepath +player.filename));
    }
    
  public Result getRedoxCSV(Integer playernumber){
    	
    	System.out.println("getCSV called");
   	 Player player = Player.findByNumber(playernumber);
   	
   	 return ok(new java.io.File(filepath +player.redoxFilename));
    }
    
    
    
    public CompletionStage<Result> uploadMultipleCSV() {
    	
    	User user = User.findByEmail(session().get("connected"));
    	List<User> allusers = User.find.all();
    	
    	List<FilePart<Object>> files = request().body().asMultipartFormData().getFiles();
    	
    	for(FilePart<Object> fp : files){
    		
    		//FilePart<File> filename = body.getFile("filename");
            System.out.println("fp iteration");
            
            if (fp != null) {
                
            	System.out.println("fp not null");
                File file = (File) fp.getFile();
                
        		CSVLoader3 csvloader = new CSVLoader3();    
        		csvloader.loadCSVFile(file.getAbsolutePath());

        		Map<String, ArrayList<String>> playerfiles = csvloader.getPlayerfilesbyname();
        		Iterator it = playerfiles.entrySet().iterator();
        		while (it.hasNext()) {
        			Map.Entry pair = (Map.Entry) it.next();
        			
        			Player player = Player.findByNameOrAlias((String) pair.getKey());
        			//player.filename = null;
        			// dumping the file into 'no players' if no play by this name found
        			if(player == null){
        				player =  Player.findByNumber(0);
        				//player.filename = null;
        			} else {
        				//player.filename = null;

            			if(player.filename == null){
            				// generate default file with headers
            				CSVTemplateGenerator cSVTemplateGenerator = new CSVTemplateGenerator();
                			player.filename = cSVTemplateGenerator.createFile(filepath, player.playername +"_"+player.playernumber+"_");
            			}
            			
            			
            			CSVAppender cSVAppender = new CSVAppender();
            			cSVAppender.updateFile(filepath + player.filename, (List<String>) pair.getValue(), true);
            		
            			
            			player.save();
        			}
        		}
    		
        		

        		
               
            } else {
            	System.out.println("fp is null");
            }
            
            
            
            List<Player> players = Player.find.all();
            for (Player player : players){
            	if(player.filename != null){
            		CSVSortByTime cSVSortByTime = new CSVSortByTime();
                	
                    cSVSortByTime.sortCSVFile(filepath + player.filename);
                    
                    player.save();
            	}
            }
            
            for (Player player : players){
            	
            	if(player.filename != null){
            		
            		AcuteChronicUpdater acuteChronicUpdater = new AcuteChronicUpdater();
        			acuteChronicUpdater.loadCSVFile(filepath + player.filename, null);
        			
        			// write out the new file
        			CSVOutput cSVOutput = new CSVOutput();
        			String newFileName = cSVOutput.writeOutFile(filepath, player.filename, acuteChronicUpdater.getPlayerfileData());
        			player.filename = newFileName;
            	}
            	player.save();
            }
    	}
    	
    	
    	
    	
    	return CompletableFuture.completedFuture(ok(users.render(user, "users", allusers)));
    }
    
    
    
   public CompletionStage<Result> uploadRedoxCSV() {
    	
	   System.out.println("uploadRedoxCSV called");
    	User user = User.findByEmail(session().get("connected"));
    	List<User> allusers = User.find.all();
    	
    	List<FilePart<Object>> files = request().body().asMultipartFormData().getFiles();
    	
    	for(FilePart<Object> fp : files){
    		
            if (fp != null) {
                
                File file = (File) fp.getFile();
                
                RedoxCSVLoader redoxcsvloader = new RedoxCSVLoader();    
                Map<String, ArrayList<String>> playerdatabyname = redoxcsvloader.getRedoxData(file.getAbsolutePath());

        		//Map<String, ArrayList<String>> playerfiles = redoxcsvloader.getPlayerfilesbyname();
        		Iterator it = playerdatabyname.entrySet().iterator();
        		while (it.hasNext()) {
        			Map.Entry pair = (Map.Entry) it.next();
        			
        			Player player = Player.findByNameOrAlias((String) pair.getKey());
        			System.out.println("player found "+player.playername);
        			for(String s : (List<String>) pair.getValue()){
        				System.out.println(s);
        			}
        			
        			// dumping the file into 'no players' if no play by this name found
        			if(player.playername.equalsIgnoreCase("no players")){
        				System.out.println("dumping");
        				player =  Player.findByNumber(0);
        				//player.filename = null;
        			} else {
        				//player.filename = null;
        				System.out.println("not dumping");
            			if(player.redoxFilename == null){
            				// generate default file with headers
            				CSVRedoxGenerator cSVRedoxGenerator = new CSVRedoxGenerator();
                			player.redoxFilename = cSVRedoxGenerator.createFile(filepath, player.playername +"_R_"+player.playernumber+"_");
            			}
            			
            			
            			CSVAppender cSVAppender = new CSVAppender();
            			cSVAppender.updateFile(filepath + player.redoxFilename, (List<String>) pair.getValue(), true);
            			
            			// using the toggleupdater to recalculate the averages
            			RedoxCSVUpdater redoxCSVUpdater = new RedoxCSVUpdater();
                		redoxCSVUpdater.toggleStateCSVFile(filepath + player.redoxFilename, "-1");
            		
            			
            			player.save();
        			}
        		}
    		
        		

        		
               
            } else {
            	System.out.println("fp is null");
            }
            
            //TODO: check that the file is in order of time
        
            
    	}
    	
    	return CompletableFuture.completedFuture(ok(users.render(user, "users", allusers)));
    }
    
    public CompletionStage<Result> updateRedoxToggleState(int playernumber, String category) {
    	
    	System.out.println("updateRedoxCSV called");
    	//

    	DynamicForm form = Form.form().bindFromRequest();
    	String timekey = form.get("timekey");

    	Player player = Player.findByNumber(playernumber);

    	// update this players redox file
    	if(player.redoxFilename != null){
    		RedoxCSVUpdater redoxCSVUpdater = new RedoxCSVUpdater();
        	
    		redoxCSVUpdater.toggleStateCSVFile(filepath + player.redoxFilename, timekey);
          }
    	
    			
    	//return CompletableFuture.completedFuture(ok(dashboard.render(user, "dashboard", player, playerIndex, players, category, categories)));
    	return CompletableFuture.completedFuture(redirect(routes.Application.dashboard(playernumber, category)));
    	
    	
    }
    
   public CompletionStage<Result> updateRedoxNote(int playernumber, String category) {
    	
    	System.out.println("updateRedoxNote called");
    	//

    	DynamicForm form = Form.form().bindFromRequest();
    	String timekey = form.get("timekey");
    	String note = form.get("note");
    	System.out.println("note before - " + note);
    	
    	note = note.replaceAll("\r\n", ":").replaceAll("\r", ":");
    	System.out.println("note after - " + note);

    	Player player = Player.findByNumber(playernumber);

    	// update this players redox file
    	if(player.redoxFilename != null){
    		RedoxCSVUpdater redoxCSVUpdater = new RedoxCSVUpdater();
        	
    		redoxCSVUpdater.addNoteCSVFile(filepath + player.redoxFilename, timekey, note);
          }
    	
    			
    	//return CompletableFuture.completedFuture(ok(dashboard.render(user, "dashboard", player, playerIndex, players, category, categories)));
    	return CompletableFuture.completedFuture(redirect(routes.Application.dashboard(playernumber, category)));
    	
    	
    }
    
    public CompletionStage<Result> uploadGamedata() {
    	
    	User user = User.findByEmail(session().get("connected"));
    	List<User> allusers = User.find.all();
    	
    	List<FilePart<Object>> files = request().body().asMultipartFormData().getFiles();
    	
    	
    	for(FilePart<Object> fp : files){
    		
    	
            if (fp != null) {
                
                File file = (File) fp.getFile();
                
                GameDataLoader gameDataLoader = new GameDataLoader();    
                gameDataLoader.loadCSVFile(file.getAbsolutePath());

                
        		Map<String, ArrayList<String>> playerfiles = gameDataLoader.getPlayerfilesbyname();
        		Iterator it = playerfiles.entrySet().iterator();
        		while (it.hasNext()) {
        			Map.Entry pair = (Map.Entry) it.next();
        			
        			Player player = Player.findByNameOrAlias((String) pair.getKey());
        			//player.filename = null;
        			// dumping the file into 'no players' if no player by this name found
        			if(player == null){
        				player =  Player.findByNumber(0);
        				//player.filename = null;
        			} else {
        				//player.filename = null;

            			if(player.filename == null){
            				// generate default file with headers
            				CSVTemplateGenerator cSVTemplateGenerator = new CSVTemplateGenerator();
                			player.filename = cSVTemplateGenerator.createFile(filepath, player.playername +"_"+player.playernumber+"_");
            			}
            			
            			
            			CSVAppender cSVAppender = new CSVAppender();
            			cSVAppender.updateFile(filepath + player.filename, (List<String>) pair.getValue(), true);
            		
            			
            			player.save();
        			}
        		}
    		
        		

        		
               
            } else {
            	System.out.println("fp is null");
            }
            
            Map<Long, List<Integer>> loads = new HashMap<Long, List<Integer>>();
            
            List<Player> players = Player.find.all();
            for (Player player : players){
            	if(player.filename != null){
            		CSVSortByTime cSVSortByTime = new CSVSortByTime();
                	
                    cSVSortByTime.sortCSVFile(filepath + player.filename);
                    
            		Map<Long, Integer> playerloads = cSVSortByTime.getLoads();
            		Iterator it = playerloads.entrySet().iterator();
            		while (it.hasNext()) {
            			Map.Entry pair = (Map.Entry) it.next();
            		// if loads already contains this key, add loads to the list
            		Long key = (Long) pair.getKey();
            		if(loads.containsKey(key)){
            			loads.get(key).add((Integer) pair.getValue());
            		} else {
            			// else put the new key along with a new list and add the load to the list
            			ArrayList<Integer> loadlist = new ArrayList<Integer>();
            			loadlist.add((Integer) pair.getValue());
            			loads.put((Long) pair.getKey(), loadlist);
            		}
            		
            		}
            			
                    player.save();
            	}
            }
            
            // calculate the averages for the game loads
            Map<Long, Integer> gameavgloads = new HashMap<Long, Integer>();
            
    		Iterator iter = loads.entrySet().iterator();
    		while (iter.hasNext()) {
    			Map.Entry pair = (Map.Entry) iter.next();
    			
    			List<Integer> vals = (List<Integer>) pair.getValue();
    			//System.out.print(pair.getKey() +" : ");
    			Integer total = 0;
    			for (Integer i : vals){
    				//System.out.print(i+", ");
    				total += i;
    			}
    			Integer avg = total/vals.size();
    			//System.out.println();
    			
    			gameavgloads.put((Long) pair.getKey(), avg);
    		}
            
            
            
            for (Player player : players){
            	
            	if(player.filename != null){
            		
            		AcuteChronicUpdater acuteChronicUpdater = new AcuteChronicUpdater();
        			acuteChronicUpdater.loadCSVFile(filepath + player.filename, gameavgloads);
        			
        			// write out the new file
        			CSVOutput cSVOutput = new CSVOutput();
        			String newFileName = cSVOutput.writeOutFile(filepath, player.filename, acuteChronicUpdater.getPlayerfileData());
        			player.filename = newFileName;
            	}
            	player.save();
            }
    	}
    	
    	
    	
    	
    	
    	
    	return CompletableFuture.completedFuture(ok(users.render(user, "users", allusers)));
    }
    
    
    
    
    
    
    
    
}
