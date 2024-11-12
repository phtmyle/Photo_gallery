package com.example.photo_gallery;

import java.util.ArrayList;
import java.util.List;

public class Album {
    private String pathFolder;
    private Image img;
    private String name;
    private List<Image> listImage;
    public Album(Image img, String name) {
        this.name = name;
        this.img = img;
        listImage = new ArrayList<>();
    }
public Album(String _name, List<Image> _listImage, String _pathFolder){
        this.name=_name;
        this.listImage= _listImage;
        this.pathFolder= _pathFolder;
        if(!_listImage.isEmpty()){
            this.img= _listImage.get(0);
        }
}
    public void setPathFolder(String _pathFolder) {
        this.pathFolder = _pathFolder;
    }
    public String getPathFolder() {
        return pathFolder;
    }

    public Image getImg() {
        return img;
    }
    public String getName() {
        return name;
    }
    public void setName(String _name){ this.name=_name;}

    public List<Image> getList() {
        return listImage;
    }
    //public void addList(List<Image> list) {
     //   listImage = new ArrayList<>(list);
   // }
    public void addItem(Image _img) {
        listImage.add(_img);
    }
}

