package cn.zhoutaolinmusic.schedule;

import cn.zhoutaolinmusic.entity.vo.HotVideo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

public class TopK {

    private int k = 0;

    private Queue<HotVideo> queue;

    /**
     * 初始化队列
     * @param k 要获取指定的k个
     * @param queue 队列
     */
    public TopK(int k, Queue<HotVideo> queue){
        this.k = k;
        this.queue = queue;
    }

    public void add(HotVideo hotVideo) {
        // 如果队列不满，直接添加
        if (queue.size() < k) {
            queue.add(hotVideo);
        } else if (queue.peek().getHot() < hotVideo.getHot()){
            // 若队列已满，和新视频比较热度，若新视频比队列中最低热度的视频要高，则替换
            queue.poll();
            queue.add(hotVideo);
        }
    }


    public List<HotVideo> get(){
        final ArrayList<HotVideo> list = new ArrayList<>(queue.size());
        while (!queue.isEmpty()) {
            list.add(queue.poll());
        }
        Collections.reverse(list);
        return list;
    }


}