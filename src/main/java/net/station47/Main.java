package net.station47;

import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Main extends Plugin {

    @Override
    public void onEnable(){
        getProxy().getScheduler().schedule(this, this::updateServersAddress, 1, 1, TimeUnit.SECONDS);
    }

    private void updateServersAddress(){
        for(Map.Entry<String, ServerInfo> server:getProxy().getServers().entrySet()){
            String address = getProxy().getConfigurationAdapter().getString("servers."+server.getKey()+".address", "");
            String host = address.split(":")[0];
            updateSocketAddress(server.getValue().getSocketAddress(), host);
        }
    }

    private void updateSocketAddress(SocketAddress address, String hostname){
        try {
            InetAddress ip = InetAddress.getByName(hostname);
            Class<? extends SocketAddress> addressClass = address.getClass();
            Field holderField = addressClass.getDeclaredField("holder");
            holderField.setAccessible(true);
            Class<?> holderClass = holderField.getType();
            Field addrField = holderClass.getDeclaredField("addr");
            addrField.setAccessible(true);
            addrField.set(holderField.get(address), ip);
            addrField.setAccessible(false);
        } catch (UnknownHostException e){
            getLogger().warning(hostname+" cannot be resolved");
        } catch (NoSuchFieldException | IllegalAccessException e){
            e.printStackTrace();
        }

    }

}
