### Modal 模态框

自定义 Modal 模态框组件

**使用方式：**

在 script 中引用组件

```javascript
import neilModal from '@/components/neil-modal/neil-modal.vue';
export default {
    components: {neilModal}
}
```

基础使用方式

```html
<neil-modal 
    :show="show" 
    @close="closeModal" 
    title="标题" 
    content="这里是正文内容，这里是正文内容，这里是正文内容，这里是正文内容，这里是正文内容，这里是正文内容"
    @cancel="bindBtn('cancel')" 
    @confirm="bindBtn('confirm')">
</neil-modal>
```

单个确认按钮

```html
<neil-modal 
    :show="show" 
    title="标题" 
    content="这里是正文内容，这里是正文内容，这里是正文内容，这里是正文内容，这里是正文内容，这里是正文内容"
    :show-cancel="false">
</neil-modal>
```

**属性说明：**

|属性名	|类型		|默认值	|说明	|
|---	|----		|---	|---	|
|title|String||标题	|
|content|String||内容|
|align|String|left|内容对齐方式，值为：left（左对齐）、center（居中对齐）、right（右对齐）|
|show   |Boolean	|false	|Modal的显示状态	|
|show-cancel|Boolean|true	|是否显示取消按钮|
|auto-close|Boolean|true	|点击遮罩是否自动关闭模态框|
|confirm-color|String|#007aff|确认按钮的颜色	|
|confirm-text|String|确定|确定按钮的文字	|
|cancel-color|String|#333333|取消按钮的颜色	|
|cancel-text|String|取消|取消按钮的文字	|

**事件说明：**

|事件名|说明		|
|close|组件关闭时触发事件|
|confirm|点击确认按钮时触发事件|
|cancel|点击取消按钮时触发事件|

**slot**

在 ``neil-modal`` 节点下，可以通过插入节点实现自定义 content 的需求（只有 content 属性为空的时候才会加载 slot）

使用示例：

```html
<neil-modal :show="show" @close="closeModal" title="更新提示" confirm-text="立即更新" cancel-text="暂不更新">
    <view style="min-height: 90upx;padding: 32upx 24upx;">
        <view>1. 修复标题颜色不对的问题</view>
        <view>2. 增加支付宝支付功能</view>
        <view>3. 增加更多示例</view>
    </view>
</neil-modal>
```

**其他**

* Modal 组件 z-index 为 1000；
* Modal 组件非原生组件，使用时会被原生组件所覆盖；
* 通过本页面下载按钮下载的zip为一个完整 ``uni-app`` 工程，拖入 HBuilderX即可运行体验效果；
* 若想集成本组件到现有工程，可以将 components 目录下的 neil-modal 目录拷贝到自己工程的 components 目录；
* 使用过程出现问题或有新的需求可在评论区留言。
