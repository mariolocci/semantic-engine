
package com.crs4.sem.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.neo4j.graphdb.Node;


import com.crs4.sem.model.Documentable;
import com.crs4.sem.neo4j.exceptions.CategoryNotFoundException;
import com.crs4.sem.neo4j.exceptions.CategoryNotFoundInTaxonomyException;
import com.crs4.sem.neo4j.exceptions.TaxonomyNotFoundException;
import com.crs4.sem.neo4j.model.CategoryNode;
import com.crs4.sem.neo4j.model.RRelationShipType;
import com.crs4.sem.neo4j.service.TaxonomyCSVReader;
import com.crs4.sem.neo4j.service.TaxonomyService;
import com.mfl.sem.classifier.model.Category;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.Data;

@Data
//@Stateless
@ApplicationScoped
@Path("/taxonomy")
@Api(value = "Taxonomy", description = "Resources api for building and organizing taxonomy classifiers")
public class TaxonomyRestResuorces {

	@Inject
	private TaxonomyService taxonomyService;

	@Inject
	private Logger log;

	@POST
	@Path("{name}/category/add/{parent}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Add partent to category", notes = "add category to partent")
	public String addCategory(@ApiParam(value = "taxonomy name" ) @DefaultValue("root") @PathParam("name") String name,@PathParam("parent") String parent_name, @ApiParam(value = "category id" ) String category_name)
			throws IOException, InterruptedException, CategoryNotFoundException {

		log.info("add category" + category_name);
		Node category = null;
		Node parent = this.getTaxonomyService().searchCategory(parent_name);
		if (parent == null)
			throw new CategoryNotFoundException();
		if (this.getTaxonomyService().searchCategory(category_name) == null)
			category = this.getTaxonomyService().createCategory(category_name);

		this.getTaxonomyService().addToParent(parent, category);
		return "ok";
	}

	@DELETE
	@Path("{name}/category/{id}")
	// @Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "delete category from parent", notes = "delete category from partent")
	public Response deleteCategory(@ApiParam(value = "taxonomy name" )   @PathParam("name") @DefaultValue("root") String name,@ApiParam(value = "parent id" ) @PathParam("id") String id, @ApiParam(value = "category id" )String category) {
		log.info("delete category " + id + " from taxonomy "+ name);
		return Response.status(200).build();
	}

	@GET
	@Path("{name}/category/branch/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	// @Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get a brach starting from a category ", notes = "Return a branch from a given category ")
	public CategoryNode getBranch(@ApiParam(value = "taxonomy name" )  @PathParam("name") @DefaultValue("root") String name, @DefaultValue("root") @ApiParam(value = "category id" ) @PathParam("id") String id) {
		log.info("get branch from taxonomy "+name +"starting from category"+ id);
		return this.taxonomyService.getBranch(name, id);
	}

	@DELETE
	@Path("{name}/keyword/{id}")
	// @Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Delete keyword from category", notes = "Delete a keyword from category ")
	public Response delete(@ApiParam(value = "taxonomy name" ) @PathParam("name") @DefaultValue("root") String name,@ApiParam(value = "category id" ) @PathParam("id") String id) {
		log.info("delete keyword "+id+" from taxonomy" + name);
		return null;
	}

	@PUT
	@Path("{name}/keyword/{id}")
	// @Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "put keyword to category", notes = "Put a keyword to category id")
	public Response putKeyword(@ApiParam(value = "taxonomy name" )  @PathParam("name") @DefaultValue("root") String name,@ApiParam(value = "category id" ) @PathParam("id") String id, @ApiParam(value = "keyword" ) String keyword) {
		log.info("put keyword "+id+"  to taxonomy" + name);
		return null;
	}
	@GET
	@Path("/{name}/keywords/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	//@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "get keywords from given category id ", notes = "Get keywords from given category id ")
	public String [] getKeyword(@ApiParam(value = "taxonomy name" )  @PathParam("name") @DefaultValue("root") String name,@ApiParam(value = "category id" ) @PathParam("id") String id ) throws CategoryNotFoundInTaxonomyException {
		log.info("getting keywords from category:"+id+"from taxonomy"+name);
		return this.taxonomyService.getKetwords(name, id);
	}

	@PUT
	@Path("/{name}/document/{id}")
	// @Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "put document to category ", notes = "Put document to category ")
	public Response putDocument(@ApiParam(value = "taxonomy name" ) @PathParam("name") @DefaultValue("root") String name,@ApiParam(value = "category id" ) @PathParam("id") String id , Documentable doc) {
		log.info("put document to taxonomy:"+name+"from category"+id);
		return null;
	}

	@DELETE
	@Path("/{name}/document/{id}")
	// @Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "delete document", notes = "Delete document from category ")
	public Response deleteDocument(@ApiParam(value = "taxonomy name" ) @PathParam("name")  @DefaultValue("root")String name,@ApiParam(value = "category id" )@PathParam("id") String id,Documentable document) {
		log.info("delete document from taxonomy:"+name+"from category"+id);
		return null;
	}

	@POST
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ApiOperation(value = "upload taxonomy", notes = "Upload a complete csv taxonomy")
	public Response uploadTaxo(@FormDataParam("file") InputStream inputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetail) throws IOException {
		log.info("added " + fileDetail.getFileName());
		TaxonomyCSVReader.readIStream(inputStream, taxonomyService);
		return Response.ok().build();

	}

	@POST
	@Path("/keywords/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ApiOperation(value = "upload  keyword set", notes = "Upload keywords to taxonomy ")
	public Response uploadKeywords(@FormDataParam("file") InputStream inputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetail) throws IOException {
		log.info("added " + fileDetail.getFileName());
		TaxonomyCSVReader.readKeywordsStream(inputStream, taxonomyService);
		return Response.ok().build();
	}
	

	@GET
	@Path("/{name}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "get  Taxonomy ", notes = "Get Taxonomy by name ")
	public String[] getTaxonomy(@DefaultValue("root") @PathParam("name") String name) throws TaxonomyNotFoundException  {
		log.info("get taxonomy "+name);
		Node root=taxonomyService.searchCategory(name);
		if(root==null) throw new com.crs4.sem.neo4j.exceptions.TaxonomyNotFoundException();
	    String[] labels = taxonomyService.branchLabels(root, true);
		return labels;
	}
	
	@GET
	@Path("/{name}/keywords")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "get  keywords Taxonomy ", notes = "Get Taxonomy Keywords by name ")
	public Set<String> getTaxonomyKeywords(@DefaultValue("root") @PathParam("name") String name) throws TaxonomyNotFoundException, CategoryNotFoundInTaxonomyException  {
		log.info("get taxonomy "+name);
		Set<String> set = taxonomyService.getAllKeywords(name, false);
		return set;
	}
	
	@DELETE
	@Path("/{name}")
	// @Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "delete document", notes = "Delete document from category id")
	public Response deleteTaxonomy(@PathParam("name") @DefaultValue("root") String name) throws com.crs4.sem.neo4j.exceptions.TaxonomyNotFoundException {
		log.info("delete taxonomy:"+name);
		this.getTaxonomyService().deleteTaxonomy( name);
		return Response.ok().build();
	}
	@POST
	@Path("/triplet/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ApiOperation(value = "upload taxonomy in triple format", notes = "Upload a complete csv taxonomy")
	public Response uploadTripletTaxo(@FormDataParam("file") InputStream inputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetail) throws IOException {
		log.info("upload taxonomy  " + fileDetail.getFileName() +" with triplet format");
		TaxonomyCSVReader.readTriple(inputStream, taxonomyService);
		return Response.ok().build();

	}

}
