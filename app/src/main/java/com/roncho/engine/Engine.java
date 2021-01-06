package com.roncho.engine;

import android.media.AudioManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.roncho.engine.android.AssetHandler;
import com.roncho.engine.android.GameRendererView;
import com.roncho.engine.android.Logger;
import com.roncho.engine.audio.AudioEngine;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public abstract class Engine extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private GameRendererView view;

    private int audioStreams = 10;

    /**
     * Called when the game should be loaded
     */
    public abstract void onLoad(World world);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        AssetHandler.init(getAssets(), getBaseContext());

        loadXml();

        AudioEngine.init(audioStreams);

        view = new GameRendererView(getBaseContext(), this);
        setContentView(view);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AudioEngine.shutdown();
    }

    private void loadXml() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(AssetHandler.loadRaw("settings.engine"));

            document.getDocumentElement().normalize();

            Node node = document.getElementsByTagName("audio").item(0);
            if(node != null){
                NodeList list = node.getChildNodes();
                for(int i = 0; i < list.getLength(); i++){
                    Node item = list.item(i);
                    if(Node.ELEMENT_NODE == item.getNodeType()){
                        Element element = (Element)item;
                        String tag = element.getTagName();
                        if(tag.equals("maxStreams")){
                            audioStreams = Integer.parseInt(element.getTextContent());
                            continue;
                        }
                        Logger.Error("No settable attribute " + tag + " under audio");
                    }
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
