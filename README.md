# plugin-thumb-picker

    引入库
    com.github.LuckSiege.PictureSelector:picture_library:v2.6.1
    com.github.bumptech.glide:glide:4.12.0
    
    注意事项
    gradle 需添加
    maven { url 'https://jitpack.io' }
    ------------
    入口的 Application
    实现 IApp 接口
    onCreate 方法中 添加 PictureAppMaster.getInstance().setApp(this);
    getPictureSelectorEngine 方法中 return new PictureSelectorEngineImp();
    end
    