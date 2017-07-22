# **pull-to-load**
a refresh framework supports any view and multiple nested views, and contains the wrapper which can load more. 

----

[ ![Download](https://api.bintray.com/packages/sunzxyong/maven/pull-to-load/images/download.svg) ](https://bintray.com/sunzxyong/maven/pull-to-load/_latestVersion)[![Travis](https://img.shields.io/travis/rust-lang/rust.svg)]() [![Hex.pm](https://img.shields.io/hexpm/l/plug.svg)]() ![](https://img.shields.io/badge/library-lightweight-yellow.svg)

## **Usage**

### **Effect**
![](https://github.com/Sunzxyong/pull-to-load/blob/master/art/refresh_2.gif)
![](https://github.com/Sunzxyong/pull-to-load/blob/master/art/refresh_3.gif)

For more effect see [Art](https://github.com/Sunzxyong/pull-to-load/tree/master/art).
### **Installation**

```
    compile 'com.zxy.android:pull-to-load:0.0.1'
```

### **Refresh**

```
    <com.zxy.android.pulltoload.refresh.PullToRefreshLayout
        android:id="@+id/refresh_layout"
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        //refresh content.

    </com.zxy.android.pulltoload.refresh.PullToRefreshLayout>
```

**PullToRefreshLayout** provides many methods can be used.

```

setRefresh(boolean refresh);//the current status.
setRefreshListener(OnRefreshListener refreshListener);//the refresh listener.
addRefreshStateListener(OnRefreshStateListener refreshStateListener);//the refresh status callback,[PREPARE,READY,REFRESHING,FINISH].
setChildScrollUpCallback(OnChildScrollUpCallback childScrollUpCallback);//whether the current view can scroll up, used to trigger the refresh.

setRefreshHeight(int height);//refresh view height.
setRefreshContainerBackground(int color);//refresh view bg.
isRefreshing();//is refreshing.
getRefreshState();//get refresh status.
setDampFactor(float factor);//set pull-down damp factor.

setRefreshView(View view);//Set up a fixed refresh the view.

//set each state to refresh the view.
setOnPrepareView(View view);
setOnReadyView(View view);
setOnRefreshingView(View view);
setOnFinishView(View view);
```

### **LoadMore**

`PullToLoad` framework provides `wrapper` for `RecyclerView` which can load more.

You don't need to change anything of your code, just need to use `RecyclerViewAdapterWrapper` wrap your adapter, like this：

```
        MyRecyclerViewAdapter realAdapter = new MyRecyclerViewAdapter();
        RecyclerViewAdapterWrapper loadMoreWrapper = new RecyclerViewAdapterWrapper(realAdapter);
        mRecyclerView.setAdapter(loadMoreWrapper);
```

`RecyclerViewAdapterWrapper` provides many methods can be used.

```
setOnLoadMoreListener(OnLoadMoreListener listener);//listen to load more status.

setupNoMoreDataStatus();//use this if there is no more data.
setupHasMoreDataStatus();//use this if there is have more data.
setupLoadFailedStatus();//use this if there load more failed.

setLoadMoreView(View view);//custom load more view.
setNoMoreView(View view);//custom no more data view.
```

The Effect:

![](https://github.com/Sunzxyong/pull-to-load/blob/master/art/loadmore.gif)

## **Version**
* **v0.0.1**:The first version.

## **License**

>
>     Apache License
>
>     Version 2.0, January 2004
>     http://www.apache.org/licenses/
>
>     Copyright 2017 郑晓勇
>
>  Licensed under the Apache License, Version 2.0 (the "License");
>  you may not use this file except in compliance with the License.
>  You may obtain a copy of the License at
>
>      http://www.apache.org/licenses/LICENSE-2.0
>
>  Unless required by applicable law or agreed to in writing, software
>  distributed under the License is distributed on an "AS IS" BASIS,
>  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
>  See the License for the specific language governing permissions and
>  limitations under the License. 

