/*
 * generated by Xtext 2.19.0
 */
package org.hpms.dab.dsl.generator

import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.core.runtime.Path
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.generator.AbstractGenerator
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtext.generator.IGeneratorContext
import org.hpms.dab.dsl.dal.DAL
import org.hpms.dab.dsl.dal.UserType
import org.hpms.dab.dsl.dal.component
import org.hpms.dab.dsl.dal.deployment
import org.hpms.dab.dsl.dal.field
import org.hpms.dab.dsl.dal.generation
import org.hpms.dab.dsl.dal.instance
import org.hpms.dab.dsl.dal.interfaceType
import org.hpms.dab.dsl.dal.offer
import org.hpms.dab.dsl.dal.process
import org.hpms.dab.dsl.dal.require
import org.hpms.dab.dsl.dal.timeout
import org.hpms.dab.dsl.dal.type
import org.hpms.dab.dsl.dal.types

/**
 * Generates code from your model files on save.
 * 
 * See https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#code-generation
 */
class DALGenerator extends AbstractGenerator {

   private def generate( type type ) {
      if( type.user !== null ) {
         if( type.user.isIsEnum || type.user.isIsAutomatonState ) {
            '''enum" userType="«type.user.name»'''
         }
         else {
            '''struct" userType="«type.user.name»'''
         }
      }
      else if( type.type == "string" ) '''string" length="«type.length»'''
      else type.type
   }

   private def generate( field field ) {
      return '''<field name="«field.name»" type="«generate( field.type )»" description="«field.description»" />
      '''
   }

   private def generateClass( UserType clazz ) {
      var result = '''      <struct name="«clazz.name»">
      '''
      for( field : clazz.fields ) {
         result += '         ' + generate( field ) 
      }
      return result + '''      </struct>
      '''
   }

   private def generateEnum( UserType enm ) {
      if( enm.isAutomatonState ) {
         return ""
      }
      var result = '''      <enumeration name="«enm.name»">
      '''
      for( literal : enm.literals ) {
         result += '''         <literal name="«literal»" />
         ''' 
      }
      return result + '''      </enumeration>
      '''
   }

   private def generate( generation generation ) {
      var result = ''
      for( language : generation.languages ) {
         result += '''      <implementation language="«language.lang»" src-dir="«language.sources»" module-name="«language.name»" />''' + '\n'
      }
      return result
   }

   private def generate( types types ) {
      if( types.classes.empty && types.classes.empty ) {
         return "";
      }
      var result = '''   <types>
      '''
      for( clazz : types.classes ) {
         result += '\n' + generateClass( clazz )
      }
      for( clazz : types.enums ) {
         result += '\n' + generateEnum( clazz )
      }
      result += generate( types.generation )
      return result + '''   </types>
      '''
   }

   private def generate( interfaceType intrfc ) {
      var result = '\n' + '''   <interface name="«intrfc.name»">''' + '\n'
      for( facet : intrfc.facets ) {
         if( facet.isRequest ) {
            result += '''      <request name="«facet.name»">''' + '\n' +
                        '         <arguments>\n'
            for( field : facet.fields ) {
               result += '            ' + generate( field )
            }
            result += '         </arguments>\n' +
                      '         <response>\n'
            for( field : facet.response ) {
               result += '            ' + generate( field )
            }
            result += '         </response>\n' +
                      '      </request>\n'
         }
         else if( facet.isData ) {
            result += '''      <data name="«facet.name»" type="«facet.type.user.name»" description="«facet.description»" />''' + '\n'
         }
         else if( facet.isIsEvent ) {
            if( facet.fields.isEmpty ) {
               result += '''      <event name="«facet.name»" />''' + '\n'
            }
            else {
               result += '''      <event name="«facet.name»">''' + '\n'
               for( field : facet.fields ) {
                  result += '         ' + generate( field )
               }
               result += '      </event>\n'
            }
         }
      }
      return result + '   </interface>\n'
   }
   
   private def generate( offer offer ) {
      '''<offers   interface="«offer.intrfc.name»" />
      '''
   }
   
   private def generate( require require ) {
      '''<requires interface="«require.intrfc.name»" />
      '''
   }
   
   private def generate( timeout to ) {
      '''<timeout name="«to.name»" duration="«to.duration»" unit="«to.unit»" />
      '''
   }
   
   private def generate( component component ) {
      var result = '\n' + '''   <component name="«component.name»"«IF component.afterDispatch»after-dispatch-needed="true"«ENDIF»>
      '''
      for( offer : component.offers ) {
         result += '      ' + generate( offer )
      }
      for( require : component.requires ) {
         result += '      ' + generate( require )
      }
      for( to : component.timeouts ) {
         result += '      ' + generate( to )
      }
      if( component.usesAutomaton ) {
         result += '''      <xi:include href="./«component.name».automaton" />
         '''
      }
      result += generate( component.generation )
      return result + '''   </component>
      '''
   }
   
   private def generate( instance instance ) {
      if( instance.requires.isEmpty ) {
         return '''         <instance name="«instance.name»" component="«instance.component.name»" />
         '''
      }
      var result = '''         <instance name="«instance.name»" component="«instance.component.name»">
      '''
      for( require : instance.requires ) {
         result += '''            <requires interface="«require.intrfc.name»" to-instance="«require.instance.name»" />
         '''
      }
      return result + '''         </instance>
      '''
   }

   private def generate( process process ) {
      var result = '';
      if( process.hostname !== null ) {
         result += '''      <process address="«process.hostname»" port="«process.port»">
         '''
      }
      else {
         result += '''      <process address="«process.ip»" port="«process.port»">
         '''
      }
      for( instance : process.instances ) {
         result += generate( instance )
      }
      return result + '''      </process>
      '''
   }

   private def generate( deployment deployment ) {
      var result = '\n' + '''   <deployment target-dir="«deployment.targetDir»">
      '''
      for( process : deployment.processes ) {
         result += generate( process )
      }
      return result + '''   </deployment>
      '''
   }
   
   private def generate( DAL model ) {
      var result = '''
      <?xml version="1.0" encoding="UTF-8"?>
      <distributed-application xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:noNamespaceSchemaLocation="distributed-application.xsd"
         xmlns:xi="http://www.w3.org/2001/XInclude"
         name="«model.name»">
      '''
      result += generate( model.types )
      for( intrfc : model.interfaces ) {
         result += generate( intrfc )
      }
      for( component : model.components ) {
         result += generate( component )
      }
      for( deployment : model.deployments ) {
         result += generate( deployment )
      }
      return result + '\n' + '''</distributed-application>
      '''
   }

	override void doGenerate( Resource resource, IFileSystemAccess2 fsa, IGeneratorContext context ) {
      val model = resource.getContents.get(0) as DAL
      val text = generate( model )
      var str = resource.URI.toPlatformString( true )
      str = str.substring( 0, str.lastIndexOf( '.' )) + '-generated.xml'
      val iFile = ResourcesPlugin.workspace.root.getFile( new Path( str ))
      val file = iFile.projectRelativePath.toPortableString
      println( file )
      fsa.generateFile( file, text )
	}
}
