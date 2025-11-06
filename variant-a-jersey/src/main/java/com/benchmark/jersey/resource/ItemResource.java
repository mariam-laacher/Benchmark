package com.benchmark.jersey.resource;

import com.benchmark.common.entity.Item;
import com.benchmark.common.dto.PageResult;
import com.benchmark.jersey.dao.ItemDAO;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Optional;

@Path("/items")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ItemResource {
    @Inject
    private ItemDAO itemDAO;

    @GET
    public Response getAll(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("categoryId") Long categoryId) {
        if (categoryId != null) {
            PageResult<Item> result = itemDAO.findByCategoryId(categoryId, page, size);
            return Response.ok(result).build();
        }
        PageResult<Item> result = itemDAO.findAll(page, size);
        return Response.ok(result).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        Optional<Item> item = itemDAO.findById(id);
        return item.map(i -> Response.ok(i).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @POST
    public Response create(@Valid Item item) {
        Item saved = itemDAO.save(item);
        return Response.status(Response.Status.CREATED).entity(saved).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Long id, @Valid Item item) {
        Optional<Item> existing = itemDAO.findById(id);
        if (existing.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        item.setId(id);
        Item updated = itemDAO.update(item);
        return Response.ok(updated).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        Optional<Item> existing = itemDAO.findById(id);
        if (existing.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        itemDAO.delete(id);
        return Response.noContent().build();
    }
}
