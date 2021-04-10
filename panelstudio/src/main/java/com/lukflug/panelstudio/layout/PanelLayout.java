package com.lukflug.panelstudio.layout;

import java.awt.Point;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.Supplier;

import com.lukflug.panelstudio.base.Animation;
import com.lukflug.panelstudio.base.IBoolean;
import com.lukflug.panelstudio.component.FocusableComponent;
import com.lukflug.panelstudio.component.IComponent;
import com.lukflug.panelstudio.container.VerticalContainer;
import com.lukflug.panelstudio.layout.ChildUtil.ChildMode;
import com.lukflug.panelstudio.popup.PopupTuple;
import com.lukflug.panelstudio.setting.IBooleanSetting;
import com.lukflug.panelstudio.setting.IClient;
import com.lukflug.panelstudio.setting.IColorSetting;
import com.lukflug.panelstudio.setting.IEnumSetting;
import com.lukflug.panelstudio.setting.IKeybindSetting;
import com.lukflug.panelstudio.setting.INumberSetting;
import com.lukflug.panelstudio.setting.ISetting;
import com.lukflug.panelstudio.theme.ITheme;
import com.lukflug.panelstudio.theme.ThemeTuple;
import com.lukflug.panelstudio.widget.Button;
import com.lukflug.panelstudio.widget.ColorComponent;
import com.lukflug.panelstudio.widget.CycleButton;
import com.lukflug.panelstudio.widget.KeybindComponent;
import com.lukflug.panelstudio.widget.NumberSlider;
import com.lukflug.panelstudio.widget.ToggleButton;

public class PanelLayout implements ILayout {
	protected int width;
	protected Point start;
	protected int skipX,skipY;
	protected Supplier<Animation> animation;
	protected IntPredicate deleteKey;
	protected IntFunction<ChildMode> layoutType;
	protected ChildMode colorType;
	protected ChildUtil util;
	
	public PanelLayout (int width, Point start, int skipX, int skipY, Supplier<Animation> animation, IntPredicate deleteKey, IntFunction<ChildMode> layoutType, ChildMode colorType, PopupTuple popupType) {
		this.width=width;
		this.start=start;
		this.skipX=skipX;
		this.skipY=skipY;
		this.animation=animation;
		this.deleteKey=deleteKey;
		this.layoutType=layoutType;
		this.colorType=colorType;
		util=new ChildUtil(width,animation,popupType);
	}
	
	@Override
	public void populateGUI(IComponentAdder gui, IClient client, ITheme theme) {
		Point pos=start;
		AtomicInteger skipY=new AtomicInteger(this.skipY);
		client.getCategories().forEach(category->{
			Button categoryTitle=new Button(category,theme.getButtonRenderer(Void.class,0,0,true));
			VerticalContainer categoryContent=new VerticalContainer(category,theme.getContainerRenderer(0,0,false));
			gui.addComponent(categoryTitle,categoryContent,new ThemeTuple(theme,0,0),new Point(pos),width,animation);
			pos.translate(skipX,skipY.get());
			skipY.set(-skipY.get());
			category.getModules().forEach(module->{
				ChildMode mode=layoutType.apply(0);
				int graphicalLevel=(mode==ChildMode.DOWN)?1:0;
				FocusableComponent moduleTitle;
				if (module.isEnabled()==null) moduleTitle=new Button(module,theme.getButtonRenderer(Void.class,1,1,mode==ChildMode.DOWN));
				else moduleTitle=new ToggleButton(module,module.isEnabled(),theme.getButtonRenderer(Boolean.class,1,1,mode==ChildMode.DOWN));
				VerticalContainer moduleContainer=new VerticalContainer(module,theme.getContainerRenderer(1,graphicalLevel,false));
				if (module.isEnabled()==null) util.addContainer(module,moduleTitle,moduleContainer,()->null,Void.class,categoryContent,gui,new ThemeTuple(theme,1,graphicalLevel),layoutType.apply(0));
				else util.addContainer(module,moduleTitle,moduleContainer,()->module.isEnabled(),IBoolean.class,categoryContent,gui,new ThemeTuple(theme,1,graphicalLevel),layoutType.apply(0));
				module.getSettings().forEach(setting->addSettingsComponent(setting,moduleContainer,gui,new ThemeTuple(theme,2,graphicalLevel+1)));
			});
		});
	}
	
	protected <T> void addSettingsComponent (ISetting<T> setting, VerticalContainer container, IComponentAdder gui, ThemeTuple theme) {
		int nextLevel=(layoutType.apply(theme.logicalLevel-1)==ChildMode.DOWN)?theme.graphicalLevel:0;
		IComponent component;
		boolean isContainer=(setting.getSubSettings()!=null)&&(layoutType.apply(theme.logicalLevel-1)==ChildMode.DOWN);
		if (setting instanceof IBooleanSetting) {
			component=new ToggleButton((IBooleanSetting)setting,theme.getButtonRenderer(Boolean.class,isContainer));
		} else if (setting instanceof INumberSetting) {
			component=new NumberSlider((INumberSetting)setting,theme.getSliderRenderer(isContainer));
		} else if (setting instanceof IEnumSetting) {
			component=new CycleButton((IEnumSetting)setting,theme.getButtonRenderer(String.class,isContainer));
		} else if (setting instanceof IColorSetting) {
			int colorLevel=(colorType==ChildMode.DOWN)?theme.graphicalLevel:0;
			VerticalContainer colorContainer=new ColorComponent((IColorSetting)setting,animation.get(),new ThemeTuple(theme,theme.logicalLevel,colorLevel));
			Button button=new Button(setting,theme.getButtonRenderer(Void.class,colorType==ChildMode.DOWN));
			util.addContainer(setting,button,colorContainer,()->setting.getSettingState(),setting.getSettingClass(),container,gui,new ThemeTuple(theme.theme,theme.logicalLevel,colorLevel),colorType);
			if (setting.getSubSettings()!=null) setting.getSubSettings().forEach(subSetting->addSettingsComponent(subSetting,colorContainer,gui,new ThemeTuple(theme,1,1)));
			return;
		} else if (setting instanceof IKeybindSetting) {
			component=new KeybindComponent((IKeybindSetting)setting,theme.getKeybindRenderer(isContainer)) {
				@Override
				public int transformKey (int scancode) {
					return deleteKey.test(scancode)?0:scancode;
				}
			};
		} else {
			component=new Button(setting,theme.getButtonRenderer(Void.class,isContainer));
		}
		if (setting.getSubSettings()!=null) {
			VerticalContainer settingContainer=new VerticalContainer(setting,theme.theme.getContainerRenderer(theme.logicalLevel,nextLevel,false));
			util.addContainer(setting,component,settingContainer,()->setting.getSettingState(),setting.getSettingClass(),container,gui,new ThemeTuple(theme.theme,theme.logicalLevel,nextLevel),layoutType.apply(theme.logicalLevel-1));
			setting.getSubSettings().forEach(subSetting->addSettingsComponent(subSetting,settingContainer,gui,new ThemeTuple(theme,1,1)));
		} else {
			container.addComponent(component);
		}
	}
}