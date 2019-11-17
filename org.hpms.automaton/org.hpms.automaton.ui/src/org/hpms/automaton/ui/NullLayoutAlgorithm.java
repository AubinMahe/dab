package org.hpms.automaton.ui;

import java.util.Comparator;
import java.util.List;

import org.eclipse.zest.layouts.Filter;
import org.eclipse.zest.layouts.InvalidLayoutConfiguration;
import org.eclipse.zest.layouts.LayoutAlgorithm;
import org.eclipse.zest.layouts.LayoutEntity;
import org.eclipse.zest.layouts.LayoutRelationship;
import org.eclipse.zest.layouts.progress.ProgressListener;

public class NullLayoutAlgorithm implements LayoutAlgorithm {

   @Override
   public void applyLayout(
      LayoutEntity[] entitiesToLayout,
      LayoutRelationship[] relationshipsToConsider,
      double x,
      double y,
      double width,
      double height,
      boolean asynchronous,
      boolean continuous ) throws InvalidLayoutConfiguration {
      // TODO Auto-generated method stub

   }

   @Override
   public boolean isRunning() {
      // TODO Auto-generated method stub
      return false;
   }

   @Override
   public void setComparator(
      Comparator comparator ) {
      // TODO Auto-generated method stub

   }

   @Override
   public void setFilter(
      Filter filter ) {
      // TODO Auto-generated method stub

   }

   @Override
   public void setEntityAspectRatio(
      double ratio ) {
      // TODO Auto-generated method stub

   }

   @Override
   public double getEntityAspectRatio() {
      // TODO Auto-generated method stub
      return 0;
   }

   @Override
   public void addProgressListener(
      ProgressListener listener ) {
      // TODO Auto-generated method stub

   }

   @Override
   public void removeProgressListener(
      ProgressListener listener ) {
      // TODO Auto-generated method stub

   }

   @Override
   public void stop() {
      // TODO Auto-generated method stub

   }

   @Override
   public void setStyle(
      int style ) {
      // TODO Auto-generated method stub

   }

   @Override
   public int getStyle() {
      // TODO Auto-generated method stub
      return 0;
   }

   @Override
   public void addEntity(
      LayoutEntity entity ) {
      // TODO Auto-generated method stub

   }

   @Override
   public void addRelationship(
      LayoutRelationship relationship ) {
      // TODO Auto-generated method stub

   }

   @Override
   public void removeEntity(
      LayoutEntity entity ) {
      // TODO Auto-generated method stub

   }

   @Override
   public void removeRelationship(
      LayoutRelationship relationship ) {
      // TODO Auto-generated method stub

   }

   @Override
   public void removeRelationships(
      List relationships ) {
      // TODO Auto-generated method stub

   }

}
