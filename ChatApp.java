import java.sql.*;
import java.net.*;
import java.io.*;
import java.lang.*;
import java.util.*;

class Connect{
static private Connection con=null;

public static Connection connect(){
try{
Class.forName("com.mysql.jdbc.Driver");
con=DriverManager.getConnection("jdbc:mysql://localhost:3306/chat","root","root");
}
catch(ClassNotFoundException e){
System.out.println("class not found exception");
e.printStackTrace();
}
catch(SQLException e){
System.out.println("database error");
//e.printStackTrace();
try{
Connect.close();
}catch(Exception ee){}
}
return con;
}

public static void close() throws Exception{
con.close();

ServerReader.br.close();
ServerWriter.dos.close();
ClientReader.br.close();
ClientWriter.dos.close();

ServerReader.t.interrupt();
ServerWriter.t.interrupt();
ClientReader.t.interrupt();
ClientWriter.t.interrupt();
System.exit(0);
}
}


class ServerReader implements Runnable{

static BufferedReader br;
static Thread t;

public ServerReader(BufferedReader br){
ServerReader.br=br;
t=new Thread(this,"Server Reader");
t.start();
}

public void run(){
String c;
try{
System.out.println("Server Reader....");
Connection con=Connect.connect();
Statement st=con.createStatement();
while((c=br.readLine())!=null){
long dt=new java.util.Date().getTime();
st.executeUpdate("insert into "+ChatApp.tablename+" values('client','"+c+"',"+dt+",'"+ChatApp.clientip.toString()+"',"+ChatApp.clientport+")");
System.out.println("Received:"+c);
}
}catch(IOException e){
System.out.println("Connection closed");
//e.printStackTrace();
try{
Connect.close();
}catch(Exception ee){}
}
catch(SQLException e){
System.out.println("database error");
e.printStackTrace();
}
}
}

class ServerWriter implements Runnable{
static DataOutputStream dos;
static Thread t;

public ServerWriter(DataOutputStream dos){
ServerWriter.dos=dos;
t=new Thread(this,"Server Writer");
t.start();
}

public void run(){
try{
System.out.println("Server Writer....");
Connection con=Connect.connect();
Statement st=con.createStatement();

while(true){
String str=System.console().readLine();
dos.writeBytes(str+"\n");
long dt=new java.util.Date().getTime();
st.executeUpdate("insert into "+ChatApp.tablename+" values('server','"+str+"',"+dt+",'"+ChatApp.serverip.toString()+"',"+ChatApp.serverport+")");
System.out.println("                   :Sent");
}
}catch(IOException e){
System.out.println("Connection closed");
//e.printStackTrace();
try{
Connect.close();
}catch(Exception ee){}
}
catch(SQLException e){
System.out.println("database error");
e.printStackTrace();
}
}
}

class ClientReader implements Runnable{
static BufferedReader br;
static Thread t;

public ClientReader(BufferedReader br){
ClientReader.br=br;
t=new Thread(this,"Client Reader");
t.start();
}

public void run(){
String c;
try{
System.out.println("Client Reader....");
while((c=br.readLine())!=null){
//c=br.readLine();
System.out.println("Received:"+c);
}
}catch(IOException e){
System.out.println("Connection closed");
//e.printStackTrace();
try{
Connect.close();
}catch(Exception ee){}
}
}
}

class ClientWriter implements Runnable{
static DataOutputStream dos;
static Thread t;

public ClientWriter(DataOutputStream dos){
ClientWriter.dos=dos;
t=new Thread(this,"Client Writer");
t.start();
}

public void run(){
try{
System.out.println("Client Writer....");
while(true){
String str=System.console().readLine();
dos.writeBytes(str+"\n");
System.out.println("                       :Sent");
}
}catch(IOException e){
System.out.println("Connection closed");
//e.printStackTrace();
try{
Connect.close();
}catch(Exception ee){}
}
}
}

class ChatApp{
static int serverport=4444;
static int clientport;
static InetAddress clientip;
static InetAddress serverip;
static String tablename;

static void server(){
try{
ServerSocket srv=new ServerSocket(serverport);
Socket skt=srv.accept();

clientip=skt.getInetAddress();
serverip=InetAddress.getLocalHost();
clientport=skt.getPort();

Connection con=Connect.connect();
Statement st=con.createStatement();
java.util.Date dt=new java.util.Date();
tablename="t"+dt.hashCode();
st.executeQuery("use chat");

String sql="insert into master values('"+tablename+"')";
st.executeUpdate(sql);

sql="create table "+tablename+"(type varchar(10),line varchar(1000),dtime bigint not null primary key,ipaddress varchar(40),port int)";
st.executeUpdate(sql);

InputStream is=skt.getInputStream();
InputStreamReader isr=new InputStreamReader(is);
BufferedReader br=new BufferedReader(isr,1024);
new ServerReader(br);

OutputStream os=skt.getOutputStream();
DataOutputStream dos=new DataOutputStream(os);
new ServerWriter(dos);
}
catch(SocketException e){
System.out.println("socket closed");
e.printStackTrace();
}
catch(IOException e){
System.out.println("Connection closed");
//e.printStackTrace();
try{
Connect.close();
}catch(Exception ee){}
}
catch(SQLException e){
System.out.println("database error");
e.printStackTrace();
}
}


static void client(){
System.out.println("Enter ipaddress to connect:");
String address=System.console().readLine();
try{
Socket skt=new Socket(address,serverport);

InputStream is=skt.getInputStream();
InputStreamReader isr=new InputStreamReader(is);
BufferedReader br=new BufferedReader(isr,1024);
new ClientReader(br);

OutputStream os=skt.getOutputStream();
DataOutputStream dos=new DataOutputStream(os);
new ClientWriter(dos);

}
catch(UnknownHostException e){
System.out.println("unknown host");
e.printStackTrace();
}
catch(IOException e){
System.out.println("Connection closed");
//e.printStackTrace();
try{
Connect.close();
}catch(Exception ee){}
}
}

public static void main(String[] arg){
if(arg.length==1){
System.out.println("Server started....");
server();
}
else{
System.out.println("Client started....");
client();
}

}
}
