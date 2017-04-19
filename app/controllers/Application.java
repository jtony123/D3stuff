package controllers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;

import org.apache.solr.client.solrj.impl.HttpSolrClient;

import com.avaje.ebean.enhance.agent.SysoutMessageOutput;
import com.fasterxml.jackson.databind.node.ObjectNode;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import models.Category;
import models.Player;
import models.Redox;
import models.User;
import play.Configuration;
import play.data.DynamicForm;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
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
import utilities.RedoxUtilities;
import utilities.RandRQueryRequest;
import utilities.WatsonDocument;
//import play.db.jpa.*;
import views.html.Admin.users;
import views.html.dashboard;
import views.html.index;
import views.html.Admin.users;
import views.html.redox;
import views.html.redoxQuestionnaire;


import com.ibm.watson.developer_cloud.conversation.v1.*;
import com.ibm.watson.developer_cloud.retrieve_and_rank.v1.RetrieveAndRank;

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
    
    

    public Result askWatson(String query) {
    	System.out.println("askWatson called");
    	User user = User.findByEmail(session().get("connected"));
    	
//       	DynamicForm requestData = formFactory.form().bindFromRequest();
//       	
//       	Map<String, String> mydata = requestData.data();
//       	
//       	System.out.println("got this query : "+mydata.get("query"));
    	
 //   	System.out.println("query is : " +query);
    	
		List<WatsonDocument> watsonResponse=null;
		
	    try {
		
			RandRQueryRequest request = new RandRQueryRequest();
			
			watsonResponse = request.searchAllDocs(query);
			
			
//			for(WatsonDocument d : watsonResponse){
//				System.out.println(d.getBodytext());
//				System.out.println();
//				System.out.println();
//			}
		

	    } catch (Exception e)  {
	    	System.out.println(e.getMessage());
	    }
	    
	    ObjectNode result = Json.newObject();
	    
	    if(watsonResponse != null){
	    	for(int i = 0; i < watsonResponse.size(); ++i){
				
				result.put("doc"+i, watsonResponse.get(i).getBodytext());
			}
	    }
		
		
		
		return ok(result);
	    
	    
    	
//    	String category = "All";
//    	Category categoryFound = Category.findByName(category);
//    	Player player;
//    	List<Player> players;
//    	Integer playernumber = 2;
//    	if(categoryFound != null){
//    		System.out.println("cat not null");
//    		players = user.getPlayersCategorisedWith(categoryFound);
//    		
//    		// check for empty category for this user
//    		if(players == null || players.isEmpty()){
//    		//empty category, return 'no players'
//    			System.out.println("players is null, sending no players");
//    			player = Player.findByPlayername("no players");
//    			players.add(player);
//    		} else {
//    		// the category is not empty
//    			System.out.println("players not null");
//    	    	if (playernumber == 0) {
//    	    		// to handle initial call
//    	    		System.out.println("handle initial call, sending player 0");
//    	    		player = players.get(0);
//    	    	} else {
//    	    		player = Player.findByNumber(playernumber);
//    	    		
//    	    		
//    	    		
//    	    		
//    	    		
//    	    		System.out.println("finding player by number");
//    	    		
//    	    		// check first if the player is null(out of range)
//    	    		if(player == null) {
//    	    			player = players.get(0);
//    	    		} else {
//    	    			// check if the player that the user had highlighted is in this category
//        	    		if (!player.categories.contains(categoryFound)){
//        	    			System.out.println("player not in this category, sending player 0");
//        	    			player = players.get(0);
//        	    		}
//        	    		
//        	    		if(players.contains(player)){
//        	    			System.out.println("player is in the list");
//        	    		} else {
//        	    			System.out.println("player not in the list");
//        	    			player = players.get(0);
//        	    		}
//        	    		
//        	    		
//    	    		}
//    	    		
//    	    		 
//    	    	}
//    		}
//    		
//    	} else {
//    		//System.out.println("cat is null");
//    		players = user.getPlayersCategorisedWith(Category.findByName("All"));
//    		player = players.get(0);
//    		category = "All";
//    	}
//    	
//    	List<Category> categories = Category.find.all();
//    	//System.out.println("player count = " + players.size());
//    	int playerIndex = players.indexOf(player);
//    	//System.out.println("......");
//    			
//    	return CompletableFuture.completedFuture(ok(dashboard.render(user, "dashboard", player, playerIndex, players, category, categories)));
//    	

    }
    
    
    
    @Restrict({@Group({"admin"})})
    public CompletionStage<Result> admin() {
    	System.out.println("get admin called");
    	User user = User.findByEmail(session().get("connected"));
    	Player player = Player.findByNumber(1);
    	List<User> allusers = User.find.all();
    	return CompletableFuture.completedFuture(ok(users.render(user, "users", player, allusers)));
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
    		System.out.println("cat not null");
    		players = user.getPlayersCategorisedWith(categoryFound);
    		
    		// check for empty category for this user
    		if(players == null || players.isEmpty()){
    		//empty category, return 'no players'
    			System.out.println("players is null, sending no players");
    			player = Player.findByPlayername("no players");
    			players.add(player);
    		} else {
    		// the category is not empty
    			System.out.println("players not null");
    	    	if (playernumber == 0) {
    	    		// to handle initial call
    	    		System.out.println("handle initial call, sending player 0");
    	    		player = players.get(0);
    	    	} else {
    	    		player = Player.findByNumber(playernumber);
    	    		
    	    		
    	    		
    	    		
    	    		
    	    		System.out.println("finding player by number");
    	    		
    	    		// check first if the player is null(out of range)
    	    		if(player == null) {
    	    			player = players.get(0);
    	    		} else {
    	    			// check if the player that the user had highlighted is in this category
        	    		if (!player.categories.contains(categoryFound)){
        	    			System.out.println("player not in this category, sending player 0");
        	    			player = players.get(0);
        	    		}
        	    		
        	    		if(players.contains(player)){
        	    			System.out.println("player is in the list");
        	    		} else {
        	    			System.out.println("player not in the list");
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
    public CompletionStage<Result> redox(int playernumber, String category) {
    	System.out.println("redox called");
    	
    	
    	
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
        	    		
        	    		if(players.contains(player)){
        	    			System.out.println("player is in the list");
        	    		} else {
        	    			System.out.println("player not in the list");
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
    	
    	
    	
    	
    	
    	
    	
//    	User user = User.findByEmail(session().get("connected"));
//    	
//    	int playernumber = 4;
//    	String category = "All";
//    	Category categoryFound = Category.findByName(category);
//    	Player player;
//    	List<Player> players = Player.find.all();
    	
    	
    	return CompletableFuture.completedFuture(ok(redox.render(user, "redox", player, playerIndex, players, category, categories)));
    	
    	
    	//return CompletableFuture.completedFuture(ok(redox.render(user, "redox", players)));
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
    	
    	List<Player> players1 = new ArrayList<Player>();
    	players1.add(player);
    	
    	List<Category> categories = Category.find.all();
    	//System.out.println("player count = " + players.size());
    	int playerIndex = players.indexOf(player);
    	
    	return CompletableFuture.completedFuture(ok(redoxQuestionnaire.render(user, "redoxQ", player, playerIndex, players1, category, categories)));
    	
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
	   	return ok(new java.io.File(filepath + "Schedule2.csv"));
    }
    
    

	// CSV file.
	public CompletionStage<Result> uploadCalender() {

		//System.out.println("uploadCalender called");

		User user = User.findByEmail(session().get("connected"));
		List<User> allusers = User.find.all();

		MultipartFormData<File> body = request().body().asMultipartFormData();
		FilePart<File> filename = body.getFile("filename");
		
		File file = new File(filepath + "Schedule2.csv");
		
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

			Player player = Player.findByNumber(1);
			flash("success", "Calendar file uploaded successfully");
			
			return CompletableFuture.completedFuture(ok(users.render(user, "users", player, allusers)));
		} else {
			Player player = Player.findByNumber(1);
			flash("error", "Missing file");
			return CompletableFuture.completedFuture(ok(users.render(user, "users", player, allusers)));
		}

	}
    
    
   

    public Result getCSV(Integer playernumber){
    	
    	System.out.println("getCSV called");
   	 Player player = Player.findByNumber(playernumber);
   	
   	 return ok(new java.io.File(filepath +player.filename));
    }
    
    

    
    
  public Result getRedoxCSV(Integer playernumber){
    	
    	System.out.println("getRedoxCSV called");
   	 
 	Player p = Player.findByNumber(playernumber);
 	List<Redox> rdx = Redox.findByPlayer(p);
 	Map<Long, String> timestamps = new TreeMap<Long, String>();
 	
 	for(Redox r : rdx) {
 		
 		Integer included = r.includeInCritDiff? 1: 0;
 		String aline = p.playername +","
 				+ r.id +","
 				+ r.rdxalertreport.result + ","
 				+ r.sportscientistComment+","
 				+ r.orrecoScientist+","
 				+ r.rdxalertreport.potentialoutcomes + ","
 				+ r.rdxalertreport.actions + ","
 				
 				+ r.rdxalertreport.sleepadvice + ","
 				+ r.rdxalertreport.trainingloadadvice + ","
 				+ r.rdxalertreport.dietaryadvice + ","
 				
 				+r.exercised+","+r.eaten+","
 				+r.exercises+","
 				+"exergym,"+"exertrain,"+"exergame,"+"exerrest,"+r.other+","
 				+r.energy.toString()+","+r.muscleSoreness.toString()+","
 				+r.fever+","+r.sorethroat+","+r.headache+","+r.jointmuscleache+","+r.diarrhea+","+","
 				+r.illness+","+r.injured+","
 				+r.additionalNotes+","
 				+r.date.getTime()+","+r.defence.toString()+","+included.toString()+","+r.defenceThreshold.toString()+","+r.stress.toString()+","+included.toString()+","+r.stressThreshold.toString();
 		
 		//System.out.println(aline);
 		
 		timestamps.put(r.date.getTime(), aline);
 	}
 	
 	
 	
 	String csvFileHeader = "playername,id,"
			+"ResultShow,NOTES,SS,POTOUT,ACTION,"
 			+"Sleepadvice,TrainingLoadAdvice,DietAdvice,"
			+"TrainedToday,AteToday,Exercises,ExerciseGym,ExerciseTraining,ExerciseGame,ExerciseRest,ExerciseOther,"
			+"EnergyLevel,MuscleSoreness,"
			+"Fever,SoreThroat,Headache,JointorMuscleAche,Diarrhea,Other,"
			+"Ill,Injured,"
			+ "testernotes,"
			+"TEST_TIME,DEFENCE,DEFENCE_INC,DEFENCE_CDT,STRESS,STRESS_INC,STRESS_CDT";
 	
 	
 	File playerfile = new File("data/attachments/"+p.playername+"_Rdx.csv");
 	
		if (!playerfile.exists()) {
			try {

				playerfile.createNewFile();
			} catch (IOException e1) {
				// TODO return an error message to the user
				e1.printStackTrace();
			}
		}
 	
		PrintWriter out1 = null;
		try {
			out1 = new PrintWriter(new BufferedWriter(new FileWriter(playerfile)));

			out1.println(csvFileHeader);
			
			for (Map.Entry<Long, String> entry : timestamps.entrySet()) {
				 //System.out.println(entry.getKey() + ": " + entry.getValue());
				  out1.println(entry.getValue());
				}


		} catch (Exception e) {
			// TODO return an error message to the user
			e.printStackTrace();
		} finally {
			out1.flush();
			out1.close();
		}
		
		
 	
 	File file = new File("/tmp/tmpCSV.csv");
 	
		if (!file.exists()) {
			try {

				file.createNewFile();
			} catch (IOException e1) {
				// TODO return an error message to the user
				e1.printStackTrace();
			}
		}
 	
		PrintWriter out = null;
		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter(file)));

			out.println(csvFileHeader);
			
			for (Map.Entry<Long, String> entry : timestamps.entrySet()) {
				 //System.out.println(entry.getKey() + ": " + entry.getValue());
				  out.println(entry.getValue());
				}


		} catch (Exception e) {
			// TODO return an error message to the user
			e.printStackTrace();
		} finally {
			out.flush();
			out.close();
		}
 	return ok(new java.io.File(file.getAbsolutePath()));
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
    	
    	
    	
    	Player player = Player.findByNumber(1);
    	return CompletableFuture.completedFuture(ok(users.render(user, "users", player, allusers)));
    }
    
    
    
   public CompletionStage<Result> uploadRedoxCSV() {
    	
	   
	System.out.println("uploadRedoxCSV called");
   	User user = User.findByEmail(session().get("connected"));
   	List<User> allusers = User.find.all();
   	
   	List<FilePart<Object>> files = request().body().asMultipartFormData().getFiles();
   	
   	for(FilePart<Object> fp : files){
   		
           if (fp != null) {
               
               File file = (File) fp.getFile();
               
               RedoxUtilities rdxutil = new RedoxUtilities();
               Map<String, ArrayList<String>> playerdatabyname = rdxutil.getDemoRedoxData(file.getAbsolutePath());
               
       		Iterator it = playerdatabyname.entrySet().iterator();
       		while (it.hasNext()) {
       			
       			
       			Map.Entry pair = (Map.Entry) it.next();
       			
       			
       			String[] tokens = null;
       			
       			for(String s : (List<String>) pair.getValue()){
       				
       				tokens = s.split(",");
       			}
       			
       			for(int i=0;i<tokens.length;++i){
       				System.out.println(i +" : " + tokens[i]);
       			}
       			
       			
       			
       			Player player = Player.findByNameOrAlias((String) pair.getKey());

       			if(player.playername.equalsIgnoreCase("no players")){
       				player =  Player.findByNumber(0);
       			} else {
       				
					Long timestamp = Long.parseLong(tokens[15]);
					Date actual = new Date(timestamp);
					System.out.println("actual date is "+actual);
					// check if already have a test for this player at this time
					Redox rdx = Redox.findByTimeKey(player, actual);//new Redox(player);
					if(rdx == null){
						System.out.println("rdx is null");
						rdx = new Redox(player, actual);
						rdx.setRedoxTestResult(tokens[2],//eaten,
								tokens[3],//exercised, 
								tokens[4],//fever, 
								tokens[5],//sorethroat, 
								tokens[6],//headache, 
								tokens[7],//jointmuscleache, 
								tokens[8],//diarrhea, 
								tokens[9],//exercises
								"",//gymweights, 
								"",//practicetraining, 
								"",//game, 
								"",//rest, 
								"",//other, 
								// remember to add illness, injury here
								Double.parseDouble(tokens[13]),//energy, 
								Double.parseDouble(tokens[14]),//muscleSoreness, 
								Double.parseDouble(tokens[20]),//stress, 
								Double.parseDouble(tokens[16]),//defence, 
								true,
								tokens[10],//illness
								tokens[11],//injured
								tokens[12]//addnotes
										);
								rdx.addComment("", "");
					}else{
						System.out.println("existing rdx result");
						rdx.setRedoxTestResult(tokens[2],//eaten,
								tokens[3],//exercised, 
								tokens[4],//fever, 
								tokens[5],//sorethroat, 
								tokens[6],//headache, 
								tokens[7],//jointmuscleache, 
								tokens[8],//diarrhea,
								tokens[9],//exercises
								"",//gymweights, 
								"",//practicetraining, 
								"",//game, 
								"",//rest, 
								"",//other, 
								// remember to add illness, injury here
								Double.parseDouble(tokens[13]),//energy, 
								Double.parseDouble(tokens[14]),//muscleSoreness, 
								Double.parseDouble(tokens[20]),//stress, 
								Double.parseDouble(tokens[16]),//defence, 
								true,
								tokens[10],//illness
								tokens[11],//injured
								tokens[12]//addnotes
										);
						rdx.addComment("", "");
					}
           			player.save();
       			}
       		}
   		
           } else {
           	System.out.println("fp is null");
           }
           
   	}
   	Player player = Player.findByNumber(1);
   	return CompletableFuture.completedFuture(ok(users.render(user, "users", player, allusers)));
//	   System.out.println("uploadRedoxCSV called");
//    	User user = User.findByEmail(session().get("connected"));
//    	List<User> allusers = User.find.all();
//    	
//    	List<FilePart<Object>> files = request().body().asMultipartFormData().getFiles();
//    	
//    	for(FilePart<Object> fp : files){
//    		
//            if (fp != null) {
//                
//                File file = (File) fp.getFile();
//                
//                RedoxUtilities rdxutil = new RedoxUtilities();
//                Map<String, ArrayList<String>> playerdatabyname = rdxutil.getRedoxData(file.getAbsolutePath());
//                
//        		Iterator it = playerdatabyname.entrySet().iterator();
//        		while (it.hasNext()) {
//        			Map.Entry pair = (Map.Entry) it.next();
//        			
//        			Player player = Player.findByNameOrAlias((String) pair.getKey());
//
//        			if(player.playername.equalsIgnoreCase("no players")){
//        				System.out.println("dumping");
//        				player =  Player.findByNumber(0);
//        			} else {
//        				System.out.println("not dumping");
//            			if(player.redoxFilename == null){
//            				player.redoxFilename = rdxutil.createFile(filepath, player.playername +"_R_"+player.playernumber+"_");
//                		}
//            			
//            			rdxutil.updateFile(filepath + player.redoxFilename, (List<String>) pair.getValue(), true);
//            			rdxutil.toggleStateCSVFile(filepath + player.redoxFilename, "-1");
//                		
//            			player.save();
//        			}
//        		}
//    		
//            } else {
//            	System.out.println("fp is null");
//            }
//            
//    	}
//    	
//    	return CompletableFuture.completedFuture(ok(users.render(user, "users", allusers)));
    }
   
   
   public CompletionStage<Result> saveRedox(int playernumber, String category) {
   	
   	System.out.println("saveRedox called");
   	
   	DynamicForm form = Form.form().bindFromRequest();
   	
   	DynamicForm requestData = formFactory.form().bindFromRequest();
   	
   	Map<String, String> mydata = requestData.data();
   	
   	System.out.println(mydata);
   	
   	Player player = Player.findByNumber(playernumber);
   	
   	String fever = "";
   	if(mydata.get("fevertoday") != null) {
   		fever += mydata.get("fevertoday").equalsIgnoreCase("on") ? "Today;": "";
   	}
   	
   	if(mydata.get("feverlastweek") != null) {
   		fever += mydata.get("feverlastweek").equalsIgnoreCase("on") ? "In the last week;": "";
   	}
   	
   	String coldsorethroat = "";
   	if(mydata.get("coldtoday") != null) {
   		coldsorethroat += mydata.get("coldtoday").equalsIgnoreCase("on") ? "Today;": "";
   	}
   	
   	if(mydata.get("coldlastweek") != null) {
   		coldsorethroat += mydata.get("coldlastweek").equalsIgnoreCase("on") ? "In the last week;": "";
   	}
   	
   	String headache = "";
   	if(mydata.get("headachetoday") != null) {
   		headache += mydata.get("headachetoday").equalsIgnoreCase("on") ? "Today;": "";
   	}
   	
   	if(mydata.get("headachelastweek") != null) {
   		headache += mydata.get("headachelastweek").equalsIgnoreCase("on") ? "In the last week;": "";
   	}
   	
   	String jointmuscleache = "";
   	if(mydata.get("muscachetoday") != null) {
   		jointmuscleache += mydata.get("muscachetoday").equalsIgnoreCase("on") ? "Today;": "";
   	}
   	
   	if(mydata.get("muscachelastweek") != null) {
   		jointmuscleache += mydata.get("muscachelastweek").equalsIgnoreCase("on") ? "In the last week;": "";
   	}
   	
   	String diarrhea = "";
   	if(mydata.get("sicktoday") != null) {
   		diarrhea += mydata.get("sicktoday").equalsIgnoreCase("on") ? "Today;": "";
   	}
   	
   	if(mydata.get("sicklastweek") != null) {
   		diarrhea += mydata.get("sicklastweek").equalsIgnoreCase("on") ? "In the last week;": "";
   	}
   	
   	String exercises = "";
   	if(mydata.get("gym") != null) {
   		exercises += mydata.get("gym").equalsIgnoreCase("on") ? "Gym weights;": "";
   	}
   	
	if(mydata.get("practice") != null) {
		exercises += mydata.get("practice").equalsIgnoreCase("on") ? "Practice;": "";
   	}
	
	if(mydata.get("game") != null) {
		exercises += mydata.get("game").equalsIgnoreCase("on") ? "Game;": "";
   	}
	
	if(mydata.get("rest") != null) {
		exercises += mydata.get("rest").equalsIgnoreCase("on") ? "Rest;": "";
   	}

	if(mydata.get("otherexercises") != null || mydata.get("otherexercises") != "") {
		exercises += mydata.get("otherexercises") +";";
   	}
	
	
	
	Double nrg = Double.parseDouble(mydata.get("nrglevel"));
	if(nrg < 0.75) {
		nrg = 0.0;
	}
	
	System.out.println("nrg level is "+nrg);
	
	
	Double musc = Double.parseDouble(mydata.get("musclevel"));
	if(musc < 0.75) {
		nrg = 0.0;
	}
	
	System.out.println("nrg level is "+nrg);
	
	Double stress = 0.0;
	if(mydata.get("stress") != null){
		if(!mydata.get("stress").isEmpty()){
			stress = Double.parseDouble(mydata.get("stress"));
		}
		
	}
	
	Double defence = 0.0;
	if(mydata.get("defence") != null){
		if(!mydata.get("defence").isEmpty()){
			defence = Double.parseDouble(mydata.get("defence"));
		}
	}
	
	
	boolean include = stress > 0.01 ? true : false;
	
	
	
	String addnotes = "";
	if(mydata.get("notes") != null || mydata.get("notes") != "") {
		
		String cleanString = mydata.get("notes").replaceAll(",", "§");
		cleanString = mydata.get("notes").replaceAll("(\\r|\\n|\\r\\n)+", ";");

		addnotes += cleanString +";";
   	}
	
   	
   	
   	
   	Redox rdx = new Redox(player, new Date());
   	rdx.setRedoxTestResult(mydata.get("eaten"), 
   			mydata.get("exercise"), 
   			fever, //fever, 
   			coldsorethroat,//sorethroat, 
   			headache,//headache, 
   			jointmuscleache,//jointmuscleache, 
   			diarrhea,//diarrhea, 
   			exercises,
   			"",//gymweights, 
   			"",//practicetraining, 
   			"",//game, 
   			"",//rest, 
   			"",//other, 
   			nrg,//energy, 
   			musc,//muscleSoreness, 
   			stress,//stress, 
   			defence,//defence, 
   			include,
   			mydata.get("illness"),
   			mydata.get("injury"),
   			addnotes
   			);//includeInCritDiff);
   	
   	
   	
   	return CompletableFuture.completedFuture(redirect(routes.Application.redox(playernumber, category)));
   	
   }
   
   
   
   
   public CompletionStage<Result> saveComment(int playernumber, String category){
	   System.out.println("saveComment called");
	   
	   	DynamicForm form = Form.form().bindFromRequest();
	   	
	   	DynamicForm requestData = formFactory.form().bindFromRequest();
	   	
	   	Map<String, String> mydata = requestData.data();
	   	
	   	System.out.println(mydata);
	   	
	   	Player player = Player.findByNumber(playernumber);
	   	
	   	String rdxid = "";
	   	if(mydata.get("rdxid") != null) {
	   		rdxid += mydata.get("rdxid");
	   	}
	   	
	   	String comment = "";
		if(mydata.get("comment") != null) {
			String cleanComment = mydata.get("comment").replace(",", "§");
			comment += cleanComment;
	   	}
		
	   	String commentBy = "";
		if(mydata.get("ssname") != null) {
			commentBy += mydata.get("ssname");
	   	}
	   	
	   	Long rid = Long.parseLong(rdxid);
	   	Redox rdx = Redox.findById(rid);
	   	rdx.addComment(comment, commentBy);
	   	
	   	
	   	
   	
   	return CompletableFuture.completedFuture(redirect(routes.Application.redox(playernumber, category)));
   }
   
   
   
   
   
    
    public CompletionStage<Result> updateRedoxToggleState(int playernumber, String category) {
    	
    	System.out.println("updateRedoxCSV called");
    	
    	DynamicForm form = Form.form().bindFromRequest();
    	String timekey = form.get("timekey");
    	
    	Date date = new Date(Long.parseLong(timekey));
    	Player player = Player.findByNumber(playernumber);
    	
    	Redox rdx = Redox.findByTimeKey(player, date);
    	rdx.toggleIncludedState();
    	
    	return CompletableFuture.completedFuture(redirect(routes.Application.redox(playernumber, category)));
    	
    }
    
   public CompletionStage<Result> updateRedoxNote(int playernumber, String category) {
    	
    	System.out.println("updateRedoxNote called");
    	
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
    	
    	
    	
    	
    	
    	Player player = Player.findByNumber(1);
    	return CompletableFuture.completedFuture(ok(users.render(user, "users", player, allusers)));
    }
    
    
    
    
    
    
    
    
}
