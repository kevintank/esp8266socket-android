package com.iotesp8266.kevin.iotesp8266;

        import java.io.BufferedOutputStream;
        import java.io.IOException;
        import java.io.InputStream;
        import java.net.InetSocketAddress;
        import java.net.Socket;
        import java.net.SocketAddress;
        import java.util.ArrayList;

        import android.app.Activity;
        import android.os.Bundle;
        import android.text.Editable;
        import android.util.Log;
        import android.view.View;
        import android.view.View.OnClickListener;
        import android.widget.AdapterView.OnItemClickListener;
        import android.widget.AdapterView;
        import android.widget.ArrayAdapter;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.ListView;
        import android.widget.TextView;

public class MainActivity extends Activity implements Runnable, OnClickListener, OnItemClickListener{
    /** Called when the activity is first created. */

    final static int TIME_OUT = 5000;
    final static byte NM_SEND_OS_TYPE = 1;
    final static byte NM_SEND_MESSAGE = 2;


    private Socket m_client_socket = null;
    private BufferedOutputStream m_out_stream = null;
    private InputStream m_in_stream = null;

    //占쏙옙占쏙옙占쏙옙
    private Thread m_client_thread;

    private ArrayAdapter<String> m_adapter;
    private ListView m_list_view;
    private TextView m_text_view;

    //占쏙옙占쏙옙占썲에占쏙옙 占쏙옙占쏙옙占�占쏙옙占쌘울옙 占쏙옙占쏙옙 占쏙옙占쏙옙
    private String m_recv_string;
    private String m_debug_string;

    // 占쏙옙占쏙옙 占쏙옙占쏙옙占쏙옙 占쌩삼옙占싹몌옙 占쌔댐옙 占쏙옙占쏙옙占쏙옙 占쏙옙占쏙옙求占�占쏙옙틴占쏙옙 占쏙옙占쏙옙
    Runnable m_debug_run = new Runnable() {
        public void run()
        {
            m_text_view.setText(m_debug_string);
        }
    };

    // 占쏙옙占쏙옙占싸븝옙占쏙옙 占쏙옙占쌘울옙占쏙옙 占쏙옙占신되몌옙 占쌔댐옙 占쏙옙占쌘울옙占쏙옙 占쏙옙占쏙옙트占썰에 占쌩곤옙占싹댐옙 占쏙옙틴占쏙옙 占쏙옙占쏙옙
    Runnable m_insert_list_run = new Runnable(){
        public void run(){

            int count = m_adapter.getCount();
            m_adapter.insert(m_recv_string, count);
            m_list_view.setSelection(count);

            EditText edit = (EditText)findViewById(R.id.id_edit);
            edit.setText("");
        }

    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 占쏙옙占쏙옙트占썰를 占쏙옙占쏙옙歐占쏙옙占쏙옙占�ArrayList 占쏙옙 占쏙옙占싼댐옙.
        ArrayList<String>list_string = new ArrayList<String>();

        // ArrayAdapter 占쏙옙 占쏙옙占싹울옙 ArrayList 占쏙옙 ListView 占쌓몌옙占쏙옙 占쏙옙쩔占�XML 占쌘드를 占쏙옙占쏙옙占싼댐옙.
        m_adapter = new ArrayAdapter<String>(this, R.layout.listview_item,list_string);
        //占쏙옙占쌀쏙옙 占쏙옙占싹울옙 占쏙옙占실듸옙 id_list 占쏙옙占�ID占쏙옙 占쏙옙占쏙옙트占썰를 占쏙옙占승댐옙.
        m_list_view = (ListView)findViewById(R.id.id_list);
        // 占쏙옙占쏙옙트占썰에 占쏙옙占쏙옙占십몌옙 占쏙옙占쏙옙磯占�
        m_list_view.setAdapter(m_adapter);
        // 占쏙옙占쏙옙트占썰에 占쏙옙占쏙옙占십몌옙 占쏙옙占쏙옙磯占�
        m_list_view.setOnItemClickListener(this);

        //占쏙옙占쌀쏙옙 占쏙옙占싹울옙 占쏙옙占실듸옙 id_tv 占쏙옙占�ID占쏙옙 占쌔쏙옙트占썰를 占쏙옙占승댐옙.
        m_text_view = (TextView) findViewById(R.id.id_tv);
        // 占쌔쏙옙트占썰에 占쏙옙占쌘울옙占쏙옙 占쏙옙占쏙옙占싼댐옙.
        m_text_view.setText("tring connect!");

        // 占쏙옙占쌀쏙옙 占쏙옙占싹울옙 占쏙옙占실듸옙 id_btn 占싱띰옙占�ID占쏙옙 占쏙옙튼占쏙옙 占쏙옙占승댐옙.
        Button send_btn = (Button) findViewById(R.id.id_btn);
        // 占쏙옙튼占쏙옙 占쏙옙占쏙옙占십몌옙 占쏙옙占쏙옙磯占�
        send_btn.setOnClickListener(this);

        // 占쏙옙占쏙옙占썲를 占쏙옙占싼댐옙.
        m_client_thread = new Thread(this);
        // 占쏙옙占쏙옙占썲를 占쏙옙占쏙옙占싼댐옙.
        m_client_thread.start();





    }


    public void run(){

        try{

            SocketAddress sock_addr = new InetSocketAddress("192.168.0.55", 4998);
            m_client_socket = new Socket();
            m_client_socket.setReceiveBufferSize(1024);
            m_client_socket.setSendBufferSize(1024);
            m_client_socket.setSoLinger(true, TIME_OUT);
            m_client_socket.setSoTimeout(1000*60*15);
            m_client_socket.connect(sock_addr, TIME_OUT);
            m_debug_string = "Connect Server!!";
            m_text_view.post(m_debug_run);

            m_out_stream = new BufferedOutputStream(m_client_socket.getOutputStream());
            m_in_stream = m_client_socket.getInputStream();

            // 바이트의 사이즈를 저장한다.
            // 안드로이드의 기반인 리눅스와 윈도우즈는 Byte Ordering 이 다르기때문에
            // 2바이트의 데이터를 송수신할 때 1바이트씩 값을 바꿔주어야 한다.
            // 리눅스는(안드로이드 기계) 빅엔디안 -> 윈도우 서버는 리틀엔디안 으로 처리 해야 하므로

            //빅엔디안을 윈도우에 맞게 리틀 엔디안 으로 변경함.
            int data_size;

            String cmd_buffer = "wO";
              data_size = cmd_buffer.length();
            Log.d("DEBUG"  , "----data_size--------> " + data_size);


            byte[] data = cmd_buffer.getBytes();

            Log.d("DEBUG"  , "------data0------> " + data[0]);
            Log.d("DEBUG"  , "------data1------> " + data[1]);

            byte[] size = new byte[2];
            size[0] = (byte)(data_size & 0x00ff);   //1을 넣고
            size[1] = (byte)((data_size & 0xff00) >> 8);       //0으로 채운다.


            int s = ((size[1] << 8) & 0xff00 ) + (size[0] & 0x00ff);
            Log.d("DEBUG"  , "------결과------> " + s);


            // #O   열기
            // #C   닫기
            // #S    현재 상태 구하기
            // #T    온도 구하기
          //  m_out_stream.write(NM_SEND_OS_TYPE);
            m_out_stream.write(size);
            m_out_stream.write(data);
            m_out_stream.flush();

            onReadStream();

        }catch(Exception e){

            m_debug_string = e.getMessage();
            m_text_view.post(m_debug_run);

        }finally{

            try{
                if(m_client_socket != null){
                    if(m_out_stream != null){
                        // 占쏙옙쩍占싣�옙占쏙옙占�占쌥는댐옙.
                        m_out_stream.close();
                    }
                    if(m_in_stream != null){
                        //占쌉뤄옙 占쏙옙트占쏙옙占쏙옙 占쌥는댐옙.
                        m_in_stream.close();
                    }

                    m_client_socket.close();
                    m_client_socket = null;
                }
            }catch(IOException e){
                //nothing
            }

        }
    }

    public void onReadStream() throws IOException{

        byte msg_id;
        byte[] size = new byte[2];


        while(!m_client_thread.isInterrupted()){

            msg_id = (byte)m_in_stream.read();

            Log.d("DEBUG", "-------------------" + msg_id);

            if(msg_id == NM_SEND_MESSAGE){
                // 크기 정보가 저장된 2바이트를 읽는다.
                if(m_in_stream.read(size) == 2){
                    // 안드로이드의 기반인 리눅스와 윈도우즈는 Byte Ordering 이 다르기때문에
                    // 2바이트의 데이터를 송수신할 때 1바이트씩 값을 바꿔주어야 한다.
                    int data_size = 0;
                    data_size = size[1];
                    data_size = data_size << 8;
                    data_size = data_size | size[0];
                    // 데이터 크기만큼 배열을 할당한다.
                    byte[] data = new byte[data_size];
                    // 데이터 크기만큼 데이터를 읽는다.
                    if(m_in_stream.read(data) == data_size){
                        // 바이트 데이터를 문자열로 변환한다.
                        m_recv_string = new String(data);
                        // 해당 문자열이 리스트뷰에 추가되도록 m_insert_list_run 인터페이스를 메인 쓰레드에 전달한다.
                        m_text_view.post(m_insert_list_run);
                    }
                }
            }
        }
    }

    public void onClick(View view){

        if(m_client_socket != null && m_client_socket.isConnected() && !m_client_socket.isClosed()){

            try{

                /*//입력문자
                EditText edit = (EditText)findViewById(R.id.id_edit);
                byte[] data = edit.getText().toString().getBytes();
                int data_size = data.length;

                // 바이트의 사이즈를 저장한다.
                // 안드로이드의 기반인 리눅스와 윈도우즈는 Byte Ordering 이 다르기때문에
                // 2바이트의 데이터를 송수신할 때 1바이트씩 값을 바꿔주어야 한다.
                byte[] size = new byte[2];
                size[0] = (byte)data_size;
                size[1] = (byte)(data_size >> 8);

                // 메세지 번호를 쓴다.
               // m_out_stream.write(NM_SEND_MESSAGE);
                // 크기 정보를 쓴다.
                m_out_stream.write(size);
                // byte 배열을 쓴다.
                m_out_stream.write(data);
                // 스트림에 쓰여진 정보를 서버로 전송한다.
                m_out_stream.flush();*/

                //쓰레드에서 처리 해야 할듯.

                int data_size;

                String cmd_buffer = "wO";
                data_size = cmd_buffer.length();
                Log.d("DEBUG"  , "----data_size--------> " + data_size);

                byte[] data = cmd_buffer.getBytes();

                byte[] size = new byte[2];
                size[0] = (byte)(data_size & 0x00ff);   //1을 넣고
                size[1] = (byte)((data_size & 0xff00) >> 8);       //0으로 채운다.

                int s = ((size[1] << 8) & 0xff00 ) + (size[0] & 0x00ff);
                Log.d("DEBUG"  , "------결과------> " + s);

                // #O   열기
                // #C   닫기
                // #S    현재 상태 구하기
                // #T    온도 구하기
                //  m_out_stream.write(NM_SEND_OS_TYPE);
                m_out_stream.write(size);
                m_out_stream.write(data);
                m_out_stream.flush();


            }catch(IOException e){

                m_text_view.setText(e.getMessage());
            }
        }else{
            Log.d("DEBUG", "---------non connect");
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // TODO Auto-generated method stub

        // 占쏙옙占쌀쏙옙 占쏙옙占싹울옙 占쏙옙占실듸옙 id_edit 占쏙옙占�ID占쏙옙 占쏙옙占쏙옙트占쌔쏙옙트占쏙옙 占쏙옙占승댐옙.
        EditText edit = (EditText)findViewById(R.id.id_edit);
        // 占쏙옙占쏙옙트占썰에占쏙옙 클占쏙옙占쏙옙 占쌓몌옙占쏙옙 TextView 占쏙옙占승뤄옙 占쏙옙쨈占�
        TextView click_item = (TextView)view;

        edit.setText(click_item.getText());

    }

    @Override
    protected void onStop() {

        if(m_client_socket == null){
            try{
                // 占쏙옙占쏙옙占�占쏙옙트占쏙옙占쏙옙 占쏙옙효화 占쌜억옙占쏙옙 占쏙옙占쏙옙占싼댐옙. 占쌩곤옙占쏙옙 占쏙옙占쏙옙占썲에占쏙옙 占쌉뤄옙 占쏙옙트占쏙옙占쏙옙 read 占쌨소드에
                // 占쏙옙占�占쏙옙占승곤옙 占심뤄옙占쌍기때占쏙옙占쏙옙 占쏙옙 占쌉쇽옙占쏙옙 호占쏙옙占싹울옙 占쏙옙트占쏙옙占쏙옙 占쏙옙효화占싼댐옙.

                if(m_in_stream != null) m_client_socket.shutdownInput();
                if(m_out_stream != null) m_client_socket.shutdownOutput();

                // 占쏙옙占쏙옙占썲가 占쏙옙占쏙옙占쏙옙占쏙옙 占쏙옙占�			 if(m_client_thread.isAlive()){
                // 占쌩곤옙占쏙옙 占쏙옙占쏙옙占썲에 占쏙옙占싶뤄옙트占쏙옙 占실댐옙.
                m_client_thread.interrupt();
                // 占쏙옙占쏙옙占썲가 占쏙옙占싶뤄옙트占쏙옙 占쏙옙占쏙옙占싹울옙 占쏙옙占쏙옙占쌀띰옙占쏙옙占쏙옙 占쏙옙摸占쏙옙占�
                m_client_thread.join();

            }catch(Exception e){

                //nothing
            }
        }

        super.onStop();
    }




}