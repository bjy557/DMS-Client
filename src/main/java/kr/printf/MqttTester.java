package kr.printf;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.internal.ExceptionHelper;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import java.lang.Thread;



/**
 * Created by nexusz99 on 9/19/16.
 */
class MqttTester extends Thread implements MqttCallback  {


    private MqttClient myClient;
    private MqttConnectOptions connOpt;

    private final String TOPIC = "test_topic";

    private int testCount = 0;
    private ArrayList<String> elapseList = new ArrayList<String>();

    String BROKER_URL = "tcp://13.124.136.96:1883";


    private final Object obj = new Object();

    public MqttTester(){}

    String m_clientid;
    int m_testCase;
    int m_pub_qos;

    public MqttTester(String clientid, int testCase, int pub_qos){
        m_clientid = clientid;
        m_testCase = testCase;
        m_pub_qos = pub_qos;

        //String BROKER_URL = "tcp://52.78.85.231:55500";

        try {
            connOpt = new MqttConnectOptions();
            connOpt.setCleanSession(true);

            MemoryPersistence persistence = new MemoryPersistence();
            myClient = new MqttClient(BROKER_URL, m_clientid, "ppiyakk2", "app1", persistence);
            myClient.setCallback(this);
            myClient.connect(connOpt);
        } catch(MqttException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    void runTester(String runMode, int testCase, int qos, int thread) {
        String clientId = runMode + System.currentTimeMillis();


        if (runMode.compareTo("subscribe") == 0) {
            runSubscriber(clientId, testCase, qos);
        } else if (runMode.compareTo("publish") == 0) {

            for(int i = 0; i < thread; i++) {
                MqttTester t = new MqttTester(clientId + "-t-" + i, testCase, qos);
                //t.setDaemon(true);
                t.start();
            }
        }

    }

    public void run() {
        runPublisher(m_clientid, m_testCase, m_pub_qos);
    }

    private void runPublisher(String clientid, int testCase, int pub_qos) {
        //System.out.println("Start Publisher!!");
        double start = System.currentTimeMillis();
        while(true){
            double now = System.currentTimeMillis();
            try {
                MqttMessage msg = new MqttMessage();
                msg.setPayload(String.valueOf(now).getBytes());
                msg.setQos(pub_qos);
                msg.setRetained(false);
                //myClient.publish(TOPIC, msg);
                Thread.sleep(10);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void runSubscriber(String clientid, int testCase, int sub_qos) {

        try {
            connOpt = new MqttConnectOptions();
            connOpt.setCleanSession(true);

            MemoryPersistence persistence = new MemoryPersistence();
            myClient = new MqttClient(BROKER_URL, m_clientid, "ppiyakk2", "app1", persistence);
            myClient.setCallback(this);
            myClient.connect(connOpt);
        } catch(MqttException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        System.out.println("Start Subscriber!!");
        try {
            myClient.subscribe(TOPIC, sub_qos);
        } catch (MqttException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        int prev=0, retry=0;
        while(prev < testCase) {
            try {
                if(prev != 0 && prev == testCount) {
                    if(retry == 10){
                        System.out.println("Subscribe test timeout");
                        break;
                    }
                    else if(retry == 0) {
                        System.out.println("No more message !! wait for timeout");
                    }
                    retry++;
                    System.out.println("Subscribe test timeout remaining : " + (10-retry) +"s");
                }
                else if(retry != 0 && prev != testCount)
                    retry = 0;
                else {
                    System.out.println("Waiting for test finish");
                }
                prev = testCount;
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("["+clientid+"] Expect : " + testCase + " / Assert : " + prev);
/*
        try {
            String filename = "result/"+clientid + "-result-ms.txt";
            File redir = new File("result");
            if(!redir.exists()) {
                redir.mkdir();
            }

            BufferedWriter bos = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename)));
            for(String d : elapseList)
            {
                bos.write(d);
                bos.newLine();
            }
            bos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
*/


        try {

            myClient.disconnect();
        } catch(Exception e) {
            e.printStackTrace();
        }

        System.out.println("Job Finished");
    }

    public void connectionLost(Throwable throwable) {

    }

    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
        double arrive = System.currentTimeMillis();
        Double sent = Double.parseDouble(new String(mqttMessage.getPayload()));
        double elapse = arrive - sent;
        String log = Double.toString(sent) + "," + Double.toString(arrive) + "," + elapse;
        elapseList.add(log);
        testCount++;
        //System.out.println(testCount + " : " + log);
    }

    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }

}
