# download cassandra
script "download_install_cassandra" do
  interpreter "bash"
  cwd "#{node[:elasticsearch][:install_dir]}"
  user "root"
  code <<-EOH
curl http://apache.mirrors.pair.com/cassandra/1.1.0/apache-cassandra-1.1.0-bin.tar.gz -o cassandra.tar.gz
mkdir cassandra
tar -zxvf cassandra.tar.gz -C cassandra --strip 1
rm cassandra.tar.gz
EOH
end

# write out our environment settings
template "cassandra.in.sh" do
  path "#{node[:cassandra][:install_dir]}/cassandra/bin/cassandra.in.sh"
  source "cassandra.in.sh.erb"
end

# write out our logging configuration
template "log4j-server.properties" do
  path "#{node[:cassandra][:install_dir]}/cassandra/conf/log4j-server.properties"
  source "log4j-server.properties.erb"
end

# write out our cassandra settings
template "cassandra.yaml" do
  path "#{node[:cassandra][:install_dir]}/cassandra/conf/cassandra.yaml"
  source "cassandra.yaml.erb"
end

# fix permissions elasticsearch
script "fix_permissions" do
  interpreter "bash"
  cwd "#{node[:cassandra][:install_dir]}"
  user "root"
  code <<-EOH
mkdir /var/lib/cassandra
chown -Rf #{node[:cassandra][:owner]}:#{node[:cassandra][:group]} /var/lib/cassandra
chmod -Rf ug+rwX /var/lib/cassandra
chown -Rf #{node[:cassandra][:owner]}:#{node[:cassandra][:group]} cassandra
chmod -Rf ug+rwX cassandra
EOH
end

# startup cassandra
script "start_cassandra" do
  interpreter "bash"
  cwd "#{node[:cassandra][:install_dir]}/cassandra"
  user node[:elasticsearch][:owner]
  code <<-EOH
bin/cassandra
exit 0
EOH
end
