WindowPanel = function(config){
  //面板的编号
  this.id = config.id;

  //面板的标题
  this.title = config.title || '';

  //承载面板的容器的编号
  this.renderTo = config.renderTo || $(document.body);

  //面板内容的宽度
  this.width = config.width || 400;

  //面板内容的高度
  this.height = config.height || 300;
  
  //内容
  this.html = config.html;

  //承载面板的容器对象
  this.render = typeof this.renderTo == 'string' ? $('#'+this.renderTo) : this.renderTo;

  //面板是否可以拖动
  this.dragable = config.dragable || false;

  //工具栏的实例化对象
  this.toolbar = config.toolbar || undefined;

  //WindowPanle是否可以添加工具栏
  this.hasToolbar = config.hasToolbar || false;

  //初始化面板
  if($('#'+this.id).length==0)
    this.init();
};

WindowPanel.prototype = {

  //初次加载渲染界面
  init : function(){

    var windowEntity = this;

    this.windowBack = $('<DIV></DIV>');
    this.windowBack.attr('id', 'WindowPanel_Back');
    this.windowBack.addClass('window_back');
    this.windowBack.css('width', this.renderTo.width());
    this.windowBack.css('height', this.renderTo.height());
    this.windowBack.appendTo(this.renderTo);

    //实例化面板的承载层
    this.windowpanel = $('<DIV></DIV>');
    this.windowpanel.attr('id',this.id);
    this.windowpanel.addClass('windowpanel');
    this.windowpanel.css('z-index',this.getActive()+1);
    this.windowpanel.appendTo(this.render);
    
    //实例化面板的标题层
    this.windowpanel_title = $('<DIV></DIV>');
    this.windowpanel_title.addClass('windowpanel_title');
    this.windowpanel_title.text(this.title);
    this.windowpanel_title.appendTo(this.windowpanel);

    //实例化面板的工具层
    this.windowpanel_toolbar = $('<DIV></DIV>');
    this.windowpanel_toolbar.addClass('windowpanel_toolbar');
    this.windowpanel_toolbar.appendTo(this.windowpanel);
    
    //实例化面板的内容层
    this.windowpanel_content = $('<DIV></DIV>');
    this.windowpanel_content.addClass('windowpanel_content');
    this.windowpanel_content.css('height',this.height);
    this.windowpanel_content.appendTo(this.windowpanel);
    
    //实例化面板的关闭按钮
    this.closer = $('<DIV></DIV>');
    this.closer.addClass('closer');
    this.closer.appendTo(this.windowpanel_title);

    //获取面板内容层的左右外补丁
    var marginWidth = parseInt(this.windowpanel_content.css('marginLeft') + this.windowpanel_content.css('marginRight'));

    //获取面板内容层的上下外补丁
    var marginHeight = parseInt(this.windowpanel_content.css('marginTop') + this.windowpanel_toolbar.css('marginBottom'));

    //设置面板的宽度和高度
    this.windowpanel.css({
      'width' : this.width+marginWidth,
      'height' : this.height+marginHeight+this.windowpanel_title.height()+6
    });

    //为面板绑定onclick事件
    this.windowpanel.click(function(){

      //获取当前面板的索引
      var position = windowEntity.render.find('.windowpanel').index(windowEntity.windowpanel[0]);

      //绑定激活事件
      windowEntity.setActive(position);
    });

    //为面板绑定关闭事件
    this.closer.click(function(){
      var position = windowEntity.render.find('.windowpanel').index(windowEntity.windowpanel[0]);
      windowEntity.kill(position);
    });

    //如果dragable为true，则面板可以移动
    if(this.dragable){
	  $.dragInit({trigger:this.windowpanel_title.attr('id'), target:this.windowpanel.attr('id')});
    }
    
    this.windowpanel_content.html(this.html);
    
    //添加工具栏
    this.addToolbar(this.toolbar);
    
    //使面板在容器中居中显示
    this.top = parseInt(this.render.get(0).clientHeight / 2) - 
      parseInt(this.windowpanel.get(0).clientHeight / 2);
    this.left = parseInt(this.render.get(0).offsetWidth / 2) - 
      parseInt(this.windowpanel.get(0).offsetWidth / 2);
    this.windowpanel.css({'top':this.top+'px','left':this.left+'px'});
    
  },

  //激活索引为position的面板，使其显示在其他面板的最上层
  //@position : 已经创建的面板集合的索引
  setActive : function(position){

    //获取指定面板的z-index
    var own_z_index = parseInt(this.render.find('.windowpanel').eq(position).css('z-index'));

    //获取面板集合的最大z-index
    var max_z_index = this.getActive();
    
    //如果选中的面板的z-index小于最大值，则将选中面板的z-index设置成最大值
    if(own_z_index < max_z_index)
      this.render.find('.windowpanel').eq(position).css('z-index',max_z_index+1);
  },

  //获得当前被激活的面板索引
  getActive : function(){

    //默认的z-index
    var max_z_index = 100001;

    //从已经创建的面板中找出z-index的最大值
    this.render.find('.windowpanel').each(function(i){
      var zIndex = parseInt($(this).css('z-index'));
      if(zIndex > max_z_index){
        max_z_index = zIndex;
      }
    });

    return max_z_index;
  },

  //关闭索引为position的面板
  //@position : 已经创建的面板集合的索引
  kill : function(position){
    var windowPanelEntity = this;
    this.render.find('.windowpanel').eq(position).fadeOut(200, function(){
      windowPanelEntity.windowBack.remove();
      windowPanelEntity.render.find('.windowpanel').eq(position).remove();
    });
  },

  //显示索引为position的面板
  //@position : 已经创建的面板集合的索引
  show : function(position){
    if(this.render.find('.windowpanel').eq(position).is(':hidden'))
      this.render.find('.windowpanel').eq(position).show();
  },

  //隐藏索引为position的面板
  //@position : 已经创建的面板集合的索引
  close : function(position){
    if(this.render.find('.windowpanel').eq(position).is(':visible'))
      this.render.find('.windowpanel').eq(position).hide();
  },

  //添加工具栏
  //@toolbar : 工具栏的实例化对象
  addToolbar : function(toolbar){
    //如果面板没有添加工具栏，并且传入的工具栏对象不为空,则添加工具栏
    if(!this.hasToolbar && toolbar){

      toolbar.renderContent = this.windowpanel_toolbar;
      toolbar.render();
      this.toolbar = toolbar;
      
      //设置工具栏的高度
      this.windowpanel.height(this.windowpanel.height() + this.windowpanel_toolbar.height());
    }
    //传入的工具栏对象不为空，则追加工具栏信息
    else if(toolbar)
    {
      for(var i = 0 ; i < toolbar.items.length ; i++){
        this.toolbar.add(toolbar.items[i]);
      }
    }
  },

  //删除工具栏
  removeToolBar : function(){

    //如果面板可以添加工具栏，并且已经添加了工具栏，并且已经创建了工具栏的容器，则清空工具栏
    if(this.hasToolbar && this.toolbar && this.windowpanel_toolbar){

      //设置面板的高度
      this.windowpanel.height(this.windowpanel.height() - this.windowpanel_toolbar.height());

      //清空面板容器的内容，但是保留面板容器
      this.windowpanel_toolbar.empty();

      //清空面板的工具栏对象
      this.toolbar = null;
    }
  }
};

//根据ID隐藏面板
WindowPanel.hideById = function(id){
  $('#WindowPanel_Back').fadeOut(200);
  $('#'+id).fadeOut(200);
};
//根据ID关闭面板
WindowPanel.killById = function(id){
  $('#WindowPanel_Back').remove();
  $('#'+id).remove();
};