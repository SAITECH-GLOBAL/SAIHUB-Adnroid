package com.linktech.saihub.util.walutils;


/**
 * Created by tromo on 2021/6/2.
 */
public class NodeListManager {
    private static NodeListManager instance;

    public static NodeListManager getInstance() {
        if (instance == null) {
            instance = new NodeListManager();
        }
        return instance;
    }

    /**
     * 保存所有链主网测试网NodeList
     *
     * @param mainNetList
     */
//    public void saveNodeList(List<NodeListResultBean.DataBean.ResultBean> mainNetList) {
//        Gson gson = new Gson();
//        String nodeListJson = gson.toJson(mainNetList);
//        MMKVManager.getInstance().saveNodeList(nodeListJson);   //加载到缓存
//    }


    /**
     * 获取所有链主网测试网NodeList
     *
     * @return
     */
//    public List<NodeListResultBean.DataBean.ResultBean> getNodeList() {
//        List<NodeListResultBean.DataBean.ResultBean> nodeListList = new ArrayList<>();
//        String nodeListJson = MMKVManager.getInstance().getNodeList();
//        if (!TextUtils.isEmpty(nodeListJson)) {
//            nodeListList = parseArray(nodeListJson, NodeListResultBean.DataBean.ResultBean.class);
//        }
//        return nodeListList;
//    }

//    /**
//     * 根据链获取主网测试网NodeList
//     *
//     * @return
//     */
//    public List<NodeBean> getNodeListByChain(int walletType) {
//        List<NodeListResultBean.DataBean.ResultBean> nodeList = getNodeList();
//        List<NodeBean> singleChainNodeList = new ArrayList<>();
//        for (int i = 0; i < nodeList.size(); i++) {
//            if (TextUtils.equals(TypesUtil.getNodeChains(walletType), nodeList.get(i).getCoin())) {
//                singleChainNodeList.addAll(nodeList.get(i).getMainUrl());
//                singleChainNodeList.addAll(nodeList.get(i).getMainUrl());
//            }
//        }
//
//        return nodeListList;
//    }

}
